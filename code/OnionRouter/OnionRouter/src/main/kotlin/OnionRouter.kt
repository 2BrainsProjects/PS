import okhttp3.Request
import okhttp3.Response
import sun.misc.Signal
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ClosedByInterruptException
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate
import java.util.UUID
import kotlin.system.exitProcess

/**
 * This class represents an Onion Router
 */
class OnionRouter(private val ip: InetSocketAddress, path: String = System.getProperty("user.dir") + "\\crypto") {
    private val timeout = 5000L
    private val pathSize = 2
    private val routersAmountRequest = 4
    private val apiUri = "http://localhost:8080/api"
    private val selector = Selector.open()
    private val socketsList = emptyList<SocketChannel>().toMutableList()
    private val crypto = Crypto(path)
    private val json = "application/json"
    private val routerUrl = "$apiUri/routers"
    private var status = 0
    private var command = ""
    private val httpUtils = HttpUtils()

    init {

        println("onion router running on port $ip")
    }

    /**
     * This function starts the onion router
     * It creates a server socket, generates a certificate signing request and creates the onion router
     * It also creates a thread to handle the connection with the client
     * and a thread to accept new clients
     * Has support to interruption of the program
     * @param pwd the password of the router's CSR
     */
    fun start(pwd: String = UUID.randomUUID().toString()) {
        val sSocket = ServerSocketChannel.open().bind(ip)
        val addrString = sSocket.localAddress.toString().drop(1)
        val finalAddr = if (addrString.count { it == ':' } > 1) "127.0.0.1:${ip.port}" else addrString

        val csr = crypto.generateClientCSR(ip.port, "router", pwd)
        val routerId = createOnionRouter(csr.joinToString("\n"), finalAddr, pwd)
        println(routerId)

        val th =
            Thread {
                try {
                    while (true) {
                        val clientSocket = sSocket.accept()
                        clientSocket.configureBlocking(false)
                        clientSocket.register(selector, SelectionKey.OP_READ)
                        socketsList.add(clientSocket)
                        selector.wakeup()
                    }
                } catch (_: ClosedByInterruptException) {
                    // ignore, this exception is thrown when the program ends
                    // so it kills both working threads, this one being the master
                } catch (e: Exception) {
                    status = 1
                }
            }
        th.start()

        try {
            Thread {
                handleConnection()
            }.start()

            Signal.handle(Signal("INT")) {
                th.interrupt()
                status = 1
                gracefullyFinalize(sSocket, routerId, pwd)
                exitProcess(0)
            }

            getInput()
        } catch (e: IOException) {
            println(e.message)
        } finally {
            th.interrupt()
            status = 1
            gracefullyFinalize(sSocket, routerId, pwd)
        }
    }

    private fun buildMessagePath(): List<Pair<Int, Pair<String, X509Certificate>>> {
        val count = getRouterCount()
        val ids = (0..count).shuffled().take(routersAmountRequest)

        val list = getRouters(ids).toList()

        val pathRouters = list.shuffled().take(pathSize)
        return pathRouters
    }

    private fun getRouters(ids: List<Int>): Map<Int, Pair<String, X509Certificate>>  {
        val nodesToConnect: MutableMap<Int, Pair<String, X509Certificate>> = mutableMapOf()
        val response = httpUtils.getRequest(json, routerUrl, hashMapOf("ids" to ids.joinToString(",")), "Error getting routers")

        val body = response.body?.string()
        requireNotNull(body)
        val formattedBody = body.split("properties").filter { it.contains("id") }

        formattedBody.forEach { se ->
            val id = se.split(',').first { it.contains("id") }.split(":").last()
            val ip = se.split(',').first { it.contains("ip") }.dropWhile { !it.isDigit() && it != '[' }.dropLast(1)
            val certificateContent = se.split(',').first { it.contains("certificate") }.dropWhile { it != '-' }.dropLastWhile { it != '-' }
            val certificate = crypto.buildCertificate(certificateContent)
            nodesToConnect[id.toInt()] = Pair(ip, certificate)
        }
        return nodesToConnect
    }

    private fun getRouterCount(): Int {
        val response = httpUtils.getRequest(json, "$routerUrl/count", null, "Error getting routers max id")

        val responseBody = response.body?.string()

        // Nao sei so depois de testar
        val routerCount = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()
        println("countRouter: $routerCount")

        require(routerCount != null) { "Error getting router count" }

        return routerCount
    }

    private fun encipherMessage(
        // Pair<ip, certificate>
        list: List<Pair<String, X509Certificate>>,
        message: String,
    ): String {
        var finalMsg = message
        for (i in 0 until list.size - 1) {
            val element = list[i]
            finalMsg = crypto.encipher(finalMsg, element.second)
            finalMsg += "||${element.first}"
        }
        finalMsg = crypto.encipher(finalMsg, list.last().second)
        return finalMsg
    }

    /**
     * Method to handle the input command
     * Possible future maintenance interface
     */
    private fun getInput() {
        /*
        u1 -> on2 -> on4 -> u3
        u1 -> on1 -> on3 -> u4
        u1 -> on2 -> on3 -> u5

        verificar se ja existe algum caminho para o primeiro onion router
        senão construir o caminho deste no para o ultimo no
        criar a cebola inteira
        abrir um socket para o primeiro no caso não haja
        enviar a mensagem V
         */
        while (true) {
            println("Command:")
            print(">")
            command = readln()
            when (command) {
                "exit" -> {
                    break
                }
                else -> {
                    println("Invalid command")
                }
            }
        }
    }

    /**
     * This function handles the connection with the client
     * The selector wake up when a new client connects and
     * reads the message from the client and processes it
     */
    private fun handleConnection() {
        while (status == 0) {
            var readyToRead = selector.select(timeout)

            if (readyToRead == 0) continue
            val keys = selector.selectedKeys()

            val iterator = keys.iterator()

            while (iterator.hasNext()) {
                val key = iterator.next()
                iterator.remove()

                if (key.isReadable) {
                    val client = key.channel() as SocketChannel

                    val msg = readFromClient(client) ?: continue

                    println("Received message: $msg")
                    processMessage(msg)

                    if (--readyToRead == 0) break
                }
            }
        }
    }

    /**
     * This function makes a request to the API to create a new onion router
     * @param csr the certificate signing request
     * @param pwd the password of the router
     * @param ip the ip of the router
     * @return the id of the router created
     * @throws Exception if the request fails
     */
    private fun createOnionRouter(
        csr: String,
        ip: String,
        pwd: String,
    ): Int {
        val registerBody = httpUtils.createBody(hashMapOf("routerCSR" to csr, "ip" to ip, "pwd" to pwd))

        val registerRequest = httpUtils.createPostRequest(json, routerUrl, registerBody)
        val registerResponse: Response

        try {
            registerResponse = httpUtils.client.newCall(registerRequest).execute()
        } catch (e: Exception) {
            println(e.message)
            throw Exception("Error creating router")
        }

        if (registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()

        val routerId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

        require(routerId != null) { "Error creating router" }

        return routerId
    }

    /**
     * This function processes the message received from the client.
     * It deciphers the message, extracts the address of the next node and sends the message to the next node
     * @param msg the message to be processed
     */
    private fun processMessage(msg: String) {
        println("processing...")
        if (msg.isBlank() || msg.isEmpty()) return
        val plainText = crypto.decipher(msg, ip.port)
        println("deciphered message: $plainText")

        val addr = plainText.split("||").last() // onion || 234 325 345 234:4363
        val newMsg = plainText.dropLastWhile { it != '|' }.dropLast(2)
        val newMsgBytes = newMsg.toByteArray(Charsets.UTF_8)

        socketsList.forEach {
            if (!it.isOpen) socketsList.remove(it)
        }

        println("-------------------------------------------------------")

        // verfificar se existe/estabelecer ligação ao nextNode
        putConnectionIfAbsent(addr)

        // socket com o próximo node                           removing prefix '/'  e.g. /127.0.0.1
        val socket =
            socketsList.firstOrNull { // /127.0.0.1:8083
                it.remoteAddress.toString().drop(1) == addr
            }

        if (socket != null) {
            writeToClient(newMsgBytes, socket)
        }
    }

    /**
     * This function adds a connection to the list of sockets if it doesn't exist
     * @param addr the address of the connection to be added
     */
    private fun putConnectionIfAbsent(addr: String) {
        if (!addr.contains(":")) return
        if (!socketsList.any { it.remoteAddress.toString().drop(1) == addr }) {
            println("sending to: $addr")
            val splitAddr = addr.split(':')
            if (splitAddr.size != 2 && splitAddr.size != 9) return
            val newAddr = InetSocketAddress(addr.dropLastWhile { it != ':' }.dropLast(1), splitAddr.last().toInt())
            val nextNode = SocketChannel.open(newAddr)
            socketsList.add(nextNode)
        }
    }

    /**
     * This function reads the message from the client
     * @param client the socket to read the message from
     * @return the message read from the client
     */
    private fun readFromClient(client: SocketChannel): String? {
        val buffer = ByteBuffer.allocate(1024)
        buffer.clear()
        var msg = ""
        var size: Int = client.read(buffer)
        if (size == -1) {
            client.close()
            socketsList.removeIf { !it.isOpen }
            return null
        }
        while (size > 0) {
            val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)
            msg += bbOutput
            buffer.flip()
            buffer.clear()
            size = client.read(buffer)
        }
        return msg
    }

    /**
     * This function writes the message to the client
     * @param msgBytes the message to be sent
     * @param socket the socket to send the message
     */
    private fun writeToClient(
        msgBytes: ByteArray,
        socket: SocketChannel,
    ) {
        var newMsgBytes = msgBytes
        val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        var msgSize = newMsgBytes.size
        while (msgSize > 0) {
            // minimum size between msgSize and DEFAULT_BUFFER_SIZE
            val min = minOf(msgSize, DEFAULT_BUFFER_SIZE)
            val chunk = newMsgBytes.copyOf(min)
            byteBuffer.put(chunk)
            msgSize -= chunk.size
            newMsgBytes = newMsgBytes.drop(min).toByteArray()
            byteBuffer.flip()

            // enviar onion2
            socket.write(byteBuffer)
            byteBuffer.clear()
        }
    }

    /**
     * This function closes all the sockets, the server socket and selector
     * @param sSocket the server socket to be closed
     */
    private fun finalizeOnionRouter(sSocket: ServerSocketChannel) {
        val keys = selector.keys()
        val iterator = keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            val client = key.channel() as SocketChannel
            client.close()
        }
        selector.close()
        sSocket.close()
    }

    /**
     * This function sends a DELETE request to the API to remove the router from the list of routers
     * and then closes the server socket
     * @param sSocket the server socket to be closed
     * @param routerId the id of the router to be removed
     * @param pwd the password of the router to be removed
     */
    private fun gracefullyFinalize(
        sSocket: ServerSocketChannel,
        routerId: Int,
        pwd: String,
    ) {
        val deleteRequest =
            Request.Builder()
                .header("Content-Type", json)
                .url("$routerUrl/$routerId?pwd=$pwd")
                .delete()
                .build()

        httpUtils.client.newCall(deleteRequest).execute()

        finalizeOnionRouter(sSocket)
    }
}

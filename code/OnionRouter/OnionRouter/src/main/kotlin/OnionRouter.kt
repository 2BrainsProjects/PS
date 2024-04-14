import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.util.UUID
import sun.misc.Signal
import java.nio.channels.ClosedByInterruptException
import kotlin.system.exitProcess

/**
 * This class represents an Onion Router
 */
class OnionRouter(private val ip : InetSocketAddress, path: String = System.getProperty("user.dir") + "\\crypto"){

    private val TIMEOUT = 5000L
    private val apiUri = "http://localhost:8080/api"
    private val selector = Selector.open()
    private val socketsList = emptyList<SocketChannel>().toMutableList()
    private val crypto = Crypto(path)
    private val JSON = "application/json"
    private val url = "$apiUri/routers"
    private var status = 0
    private var command = ""
    private val client = OkHttpClient()

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
    fun start(pwd: String = UUID.randomUUID().toString()){
        val sSocket = ServerSocketChannel.open().bind(ip)
        val csr = crypto.generateClientCSR(ip.port, sSocket.localAddress.toString(), pwd)
        val routerId = createOnionRouter(csr.joinToString ( "\n" ), sSocket.localAddress.toString(), pwd)
        println(routerId)

        val th = Thread{
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

        } catch(e: IOException) {
            println(e.message)
        } finally {
            th.interrupt()
            status = 1
            gracefullyFinalize(sSocket, routerId, pwd)
        }
    }

    /**
     * Method to handle the input command
     * Possible future maintenance interface
     */
    private fun getInput(){
        while (true){
            print("Command:")
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
            var readyToRead = selector.select(TIMEOUT)

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
    private fun createOnionRouter(csr: String, ip: String, pwd: String): Int{
        val registerBody = createBody(hashMapOf("routerCSR" to csr, "ip" to ip, "pwd" to pwd))

        val registerRequest = createPostRequest(JSON, url, registerBody)
        val registerResponse: Response

        try {
            registerResponse = client.newCall(registerRequest).execute()
        } catch (e: Exception) {
            println(e.message)
            throw Exception("Error creating router")
        }

        if(registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()

        val routerId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

        require(routerId != null){ "Error creating router" }

        return routerId
    }

    /**
     * This function creates a body for the request
     * @param fields the fields of the body
     * @return the body created
     */
    private fun createBody(fields: HashMap<String, String>): FormBody {
        val formBody =  FormBody.Builder()
        fields.forEach { (k, v) -> formBody.add(k, v) }
        return formBody.build()
    }

    /**
     * This function create a post request to the API
     * @param mediaType the media type of the request
     * @param url the url of the request
     * @param body the body of the request
     * @return the request created
     */
    private fun createPostRequest(mediaType: String, url: String, body: FormBody) =
        Request.Builder()
            .header("Content-Type", mediaType)
            .url(url)
            .post(body)
            .build()

    /**
     * This function processes the message received from the client.
     * It deciphers the message, extracts the address of the next node and sends the message to the next node
     * @param msg the message to be processed
     */
    private fun processMessage(msg:String){
        println("processing...")
        if(msg.isBlank() || msg.isEmpty()) return
        val plainText = crypto.decipher(msg, ip.port)
        println("deciphered message: $plainText")

        val addr = plainText.split("||").last() // onion || 234 325 345 234:4363
        val newMsg = plainText.dropLastWhile { it != '|' }.dropLast(2)
        val newMsgBytes = newMsg.toByteArray(Charsets.UTF_8)

        socketsList.removeAll {
            it.close()
            !it.isOpen
        }

        // verfificar se existe/estabelecer ligação ao nextNode
        putConnectionIfAbsent(addr)

        // socket com o próximo node                           removing prefix '/'  e.g. /127.0.0.1
        val socket = socketsList.firstOrNull { it.remoteAddress.toString().drop(1) == addr }

        if(socket != null)
            writeToClient(newMsgBytes, socket)
    }

    /**
     * This function adds a connection to the list of sockets if it doesn't exist
     * @param addr the address of the connection to be added
     */
    private fun putConnectionIfAbsent(addr: String){
        if(!addr.contains(":")) return
        if(!socketsList.any { it.remoteAddress.toString().drop(1) == addr }){
            println("sending to: $addr")
            val splitAddr = addr.split(':')
            if (splitAddr.size != 2 && splitAddr.size != 9) return
            // testar isto
            val newAddr = InetSocketAddress(addr.drop(1).dropLastWhile { it != ':' }.dropLast(1), splitAddr.last().toInt())
            val nextNode = SocketChannel.open(newAddr)
            socketsList.add(nextNode)
        }
    }

    /**
     * This function reads the message from the client
     * @param client the socket to read the message from
     * @return the message read from the client
     */
    private fun  readFromClient(client: SocketChannel): String? {
        val buffer = ByteBuffer.allocate(1024)
        buffer.clear()
        var msg = ""
        var size: Int = client.read(buffer)
        if(size == -1) {
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
    private fun writeToClient(msgBytes: ByteArray, socket: SocketChannel){
        var newMsgBytes = msgBytes
        val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        var msgSize = newMsgBytes.size
        while (msgSize > 0){
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
    private fun finalizeOnionRouter(sSocket: ServerSocketChannel){
        val keys = selector.keys()
        val iterator = keys.iterator()
        while(iterator.hasNext()){
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
    private fun gracefullyFinalize(sSocket: ServerSocketChannel, routerId: Int, pwd: String){
        val deleteRequest = Request.Builder()
            .header("Content-Type", JSON)
            .url("$url/$routerId?pwd=$pwd")
            .delete()
            .build()

        client.newCall(deleteRequest).execute()

        finalizeOnionRouter(sSocket)
        println("end")
    }
}

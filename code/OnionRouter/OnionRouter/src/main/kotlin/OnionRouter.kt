import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
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

class OnionRouter(private val port : Int, path: String = System.getProperty("user.dir") + "\\crypto"){

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
        require(port >= 0) { "Port must not be negative" }
        println("onion router running on port $port")
    }

    fun start(pwd: String = UUID.randomUUID().toString()){

        val sSocket = ServerSocketChannel.open().bind(InetSocketAddress(port))

        val csr = crypto.generateClientCSR(port, sSocket.localAddress.toString(), pwd)

        val registerBody = FormBody.Builder()
            .add("routerCSR", csr.joinToString("\n"))
            .add("ip", sSocket.localAddress.toString())
            .add("pwd", pwd)
            .build()

        val registerRequest = Request.Builder()
            .header("Content-Type", JSON)
            .url(url)
            .post(registerBody)
            .build()
        val registerResponse: okhttp3.Response
        try {
            registerResponse = client.newCall(registerRequest).execute()
        } catch (e: Exception) {
            throw Exception("Error creating router")
        }
        if(registerResponse.code != 201) throw Exception("Error creating router")

        val responseBody = registerResponse.body?.string()


        val routerId = responseBody?.split(',')?.get(1)?.dropWhile { !it.isDigit() }?.takeWhile { it.isDigit() }?.toIntOrNull()

        require(routerId != null){ "Error creating router" }

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

            while (true){
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

        } catch(e: IOException) {
            println(e.message)
        } finally {
            th.interrupt()
            status = 1
            gracefullyFinalize(sSocket, routerId, pwd)
        }
    }

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
                    processMessage(msg, socketsList)

                    if (--readyToRead == 0) break
                }
            }
        }
    }

    //                          onion
    private fun processMessage(msg:String, socketsList: MutableList<SocketChannel>){
        println("processing...")
        if(msg.isBlank() || msg.isEmpty()) return
        val plainText = crypto.decipher(msg, port)
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

    private fun putConnectionIfAbsent(addr: String){
        if(!addr.contains(":")) return
        if(!socketsList.any { it.remoteAddress.toString().drop(1) == addr }){
            println("sending to: $addr")
            val splitAddr = addr.split(':')
            if (splitAddr.size != 2) return
            val newAddr = InetSocketAddress(splitAddr[0], splitAddr[1].toInt())
            val nextNode = SocketChannel.open(newAddr)
            socketsList.add(nextNode)
        }
    }
    
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

import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

class OnionRouter(private val port : Int){

    init {
        require(port >= 0) { "Port must not be negative" }
    }

    private val selector = Selector.open()
    private val socketsList = emptyList<SocketChannel>().toMutableList()
    private val crypto = Crypto()

    fun start(){
        val sSocket = ServerSocketChannel.open()
        sSocket.socket().bind(InetSocketAddress(port))

        val csr = crypto.generateClientCSR(port, sSocket.localAddress.toString(), "password")

        // gerar chave pública e privada

        try {
            Thread{
                handleConnection()
            }.start()
            while(true){
                println("waiting to accept")
                val clientSocket = sSocket.accept()
                clientSocket.configureBlocking(false)
                clientSocket.register(selector, SelectionKey.OP_READ)
                socketsList.add(clientSocket)
                println("waking up selector")
                selector.wakeup()
            }
        } catch(e: IOException) {
            println(e.message)
        } finally {
            finalizeOnionRouter(sSocket)
        }
    }

    // para o cliente encriptar com a chave pública
    private fun generateKeyPair(localAddress: InetSocketAddress){
        TODO("Not yet implemented")
    }

    private fun createCSR(localAddress: InetSocketAddress){
        TODO("Not yet implemented")
    }

    private fun handleConnection() {
        println("entered handleConnection")
        while (true){
            var readyToRead = selector.select()

            if(readyToRead == 0) continue
            val keys = selector.selectedKeys()

            val iterator = keys.iterator()

            while(iterator.hasNext()) {
                val key = iterator.next()
                iterator.remove()

                if (key.isReadable) {
                    val client = key.channel() as SocketChannel

                    val msg = readFromClient(client) ?: continue

                    //println("Processing msg: $msg")
                    processMessage(msg, socketsList)
                    println("Passed processMsg")

                    if (--readyToRead == 0) break
                }
            }
        }
    }

    //                          onion
    // adicionar o porto aos routers na db ou aumentar o tamanho do ip
    private fun processMessage(msg:String, socketsList: MutableList<SocketChannel>){
        println("entered processMsg")
        if(msg.isBlank() || msg.isEmpty()) return

        val plainText = crypto.decryptMessage(port, msg)

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
        val socket = socketsList.first { it.remoteAddress.toString().drop(1) == addr }

        writeFromClient(newMsgBytes, socket)
    }

    private fun putConnectionIfAbsent(addr: String){
        if(!socketsList.any { it.remoteAddress.toString().drop(1) == addr }){
            println(addr)
            val splitAddr = addr.split(':')
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

    private fun writeFromClient(msgBytes: ByteArray, socket: SocketChannel){
        var newMsgBytes = msgBytes
        val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        var msgSize = newMsgBytes.size
        while (msgSize > 0){
            println("cicle processMsg")
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
}

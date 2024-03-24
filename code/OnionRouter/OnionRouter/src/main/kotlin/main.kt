import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

fun main() {
//    require(args.size == 1) { "Missing port number" }
    val port = 8082 //args.first().toIntOrNull()
    require(port != null) { "Invalid port" }
    require(port >= 0) { "Port must not be negative" }

    val sSocket = ServerSocketChannel.open()
    val cSelector = Selector.open()
    sSocket.socket().bind(InetSocketAddress(port))
    val sockets = emptyList<SocketChannel>().toMutableList()
    try {
        println("passing to thread")
        Thread{
            handleConnection(cSelector, sockets)
        }.start()
        while(true){
            println("waiting to accept")
            val clientSocket = sSocket.accept()
            clientSocket.configureBlocking(false)
            clientSocket.register(cSelector, SelectionKey.OP_READ)
            sockets.add(clientSocket)
            println("waking up selector")
            cSelector.wakeup()
        }
    } catch(e: IOException) {
        println(e.message)
    } finally {
        finalizeOnionRouter(sSocket, cSelector)
    }
}

private fun handleConnection(selector: Selector, socketsList: MutableList<SocketChannel>) {
    println("entered handleConnection")
    while (true){
        var readyToRead = selector.select(1000)

        if(readyToRead == 0) {
            selector.wakeup()
            continue
        }
        val keys = selector.selectedKeys()
        println(keys.size)
        val iterator = keys.iterator()

        while(iterator.hasNext()) {
            val key = iterator.next()
            iterator.remove()

            if (key.isReadable) {
                val client = key.channel() as SocketChannel
                val buffer = ByteBuffer.allocate(1024)
                buffer.clear()
                var msg = ""
                while (client.read(buffer) > 0) {
                    val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)
                    msg += bbOutput
                    buffer.flip()
                    buffer.clear()
                }

                println("Processing msg: $msg")
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
//    val keyPath = System.getProperty("user.dir") + "\\key"
//    // leitura da chave privada
//    val keyStream = FileInputStream(keyPath)
//    val keyBytes = keyStream.readBytes()
//    val privateKeySpec = PKCS8EncodedKeySpec(keyBytes)
//    val keyFactory = KeyFactory.getInstance("RSA")
//    val privateKey = keyFactory.generatePrivate(privateKeySpec)
//
//    val cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding")
//    cipher.init(Cipher.DECRYPT_MODE, privateKey)
//
//    // decifrar a mensagem
//    val decryptedMsg = cipher.doFinal(msg.toByteArray())
//
    val plaintext = msg //decryptedMsg.decodeToString()
    val addr = plaintext.split("||").last() // onion || 234 325 345 234:4363
    val newMsg = plaintext.dropLastWhile { it != '|' }.dropLast(2)
    var newMsgBytes = newMsg.toByteArray(Charsets.UTF_8)

    socketsList.removeIf { !it.isOpen }

    socketsList.forEach{
        println(it.toString())
    }

    // verfificar se existe/estabelecer ligação ao nextNode
    if(!socketsList.any { it.remoteAddress.toString().drop(1) == addr }){
        val splittedAddr = addr.split(':')
        println(addr)
        val newAddr = InetSocketAddress(splittedAddr[0], splittedAddr[1].toInt())
        val nextNode = SocketChannel.open(newAddr)
        socketsList.add(nextNode)
    }

    // socket com o próximo node                           removing prefix '/'  e.g. /127.0.0.1
    val socket = socketsList.first { it.remoteAddress.toString().drop(1) == addr }

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

private fun finalizeOnionRouter(sSocket: ServerSocketChannel, selector: Selector){
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

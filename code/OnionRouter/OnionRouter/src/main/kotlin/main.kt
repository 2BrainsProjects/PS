import java.io.FileInputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

fun main(args: Array<String>) {
    require(args.size == 1) { "Missing port number" }
    val port = args.first().toIntOrNull()
    require(port != null) { "Invalid port" }
    require(port >= 0) { "Port must not be negative" }

    val sSocket = ServerSocketChannel.open()
    val selector = Selector.open()
    val sockets = emptyList<SocketChannel>().toMutableList()

    try {
        sSocket.socket().bind(InetSocketAddress(port))
        println("passing to thread")
        Thread{ handleConnection(selector, sockets) }.start()
        while(true){

            val clientSocket = sSocket.accept()

            clientSocket.configureBlocking(false)
            clientSocket.register(selector, SelectionKey.OP_READ)
            sockets.add(clientSocket)
        }

    } catch(e: IOException) {
        println(e.message)
    } finally {
        finalizeOnionRouter(sSocket, selector)
    }
}

private fun handleConnection(selector: Selector, socketsList: MutableList<SocketChannel>) {
    while (true){
        var readyToRead = selector.select()

        if(readyToRead == 0) continue
        val keys = selector.keys()
        val iterator = keys.iterator()

        while(iterator.hasNext()){
            val key = iterator.next()
            if(key.isReadable){
                val client = key.channel() as SocketChannel
                val buffer = ByteBuffer.allocate(1024)
                var msg= ""
                while(client.read(buffer) >= 0){
                    buffer.flip()
                    msg += Charsets.UTF_8.decode(buffer).toString()
                    buffer.clear()
                }
                //fazer cenas
                processMessage(msg, socketsList)

                if (--readyToRead == 0) break
            }
        }
    }
}

//                          onion

// adicionar o porto aos routers na db ou aumentar o tamanho do ip
private fun processMessage(msg:String, socketsList: MutableList<SocketChannel>){
    val keyPath = System.getProperty("user.dir") + "\\key"
    // leitura da chave privada
    val keyStream = FileInputStream(keyPath)
    val keyBytes = keyStream.readBytes()
    val privateKeySpec = PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKey = keyFactory.generatePrivate(privateKeySpec)

    val cipher = Cipher.getInstance("RSA/CBC/PKCS1Padding")
    cipher.init(Cipher.DECRYPT_MODE, privateKey)

    // decifrar a mensagem
    val decryptedMsg = cipher.doFinal(msg.toByteArray())

    val plaintext = decryptedMsg.decodeToString()
    val addr = plaintext.split("||").last() // onion || 234 325 345 234:4363
    val newMsg = plaintext.dropLastWhile { it != '|' }.dropLast(2)
    var newMsgBytes = newMsg.toByteArray(Charsets.UTF_8)

    // verfificar se existe/estabelecer ligação ao nexNode
    if(!socketsList.any { it.remoteAddress.toString() == addr }){
        val nextNode = SocketChannel.open()
        val splittedAddr = addr.split(':')
        nextNode.socket().bind(InetSocketAddress(splittedAddr[0], splittedAddr[1].toInt()))

        socketsList.add(nextNode)
    }

    // socket com o próximo node
    val socket = socketsList.first { it.remoteAddress.toString() == addr }

    val byteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
    var msgSize = newMsgBytes.size
    while (msgSize > 0){
        val chunk = newMsgBytes.copyOf(DEFAULT_BUFFER_SIZE)
        byteBuffer.put(chunk)
        msgSize -= chunk.size
        newMsgBytes = newMsgBytes.drop(DEFAULT_BUFFER_SIZE).toByteArray()
    }

    // enviar onion2
    socket.write(byteBuffer)
}

private fun finalizeOnionRouter(sSocket: ServerSocketChannel, selector: Selector){
    sSocket.close()
    val keys = selector.keys()
    val iterator = keys.iterator()
    while(iterator.hasNext()){
        val key = iterator.next()
        val client = key.channel() as SocketChannel
        client.close()
    }
    selector.close()
}

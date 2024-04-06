import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.nio.charset.StandardCharsets

fun clientReceiver(ip: InetSocketAddress, pwd: String, path: String = System.getProperty("user.dir") + "\\crypto"): String{
    val crypto = Crypto(path)
    val lastClient = ServerSocketChannel.open()
    lastClient.socket().bind(ip)
    var socket : SocketChannel? = null
    var strRead = ""

    val csr = crypto.generateClientCSR(ip.port, lastClient.localAddress.toString(), pwd)

    try {
        println("waiting to accept")
        socket = lastClient.accept()
        println("accepted")
        strRead = readFromClient(socket, ip.port, path)
    }catch (e: Exception){
        println(e.message)
    }finally {
        lastClient.close()
        socket?.close()
    }
    return strRead
}

private fun readFromClient(client: SocketChannel, port:Int, path: String): String{
    val buffer = ByteBuffer.allocate(1024)
    buffer.clear()
    var msg = ""
    var size: Int = client.read(buffer)
    if(size == -1) {
        client.close()
        return msg
    }

    println("reading")
    while (size > 0) {
        val bbOutput = String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8)
        buffer.flip()
        msg += bbOutput
        buffer.clear()
        size = client.read(buffer)
    }

    val decipherMsg = Crypto(path).decipher(msg, port)
    println(decipherMsg)
    println("____________________")
    return decipherMsg
}

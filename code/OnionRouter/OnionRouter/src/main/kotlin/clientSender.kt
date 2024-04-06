import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(InetSocketAddress(8080), "password", "hello", listOf("127.0.0.1:8081", "127.0.0.1:8082"))
}

fun clientSender(
    serverIp: InetSocketAddress,
    pwd: String,
    msg: String,
    nodes: List<String>,
    certificatePath: String = System.getProperty("user.dir") + "\\crypto"
): Int{
    val socketChannel = SocketChannel.open(serverIp)
    var bytesWritten: Int

    try {
        val crypto = Crypto(certificatePath)
        val ip = socketChannel.socket().localAddress.toString()
        val csrOutput = crypto.generateClientCSR(socketChannel.socket().port, ip, pwd)
        println("message: $msg")

        var finalMsg = msg
        nodes.reversed().forEach{
            val port = it.split(":")[1].toInt()
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$it"
            println(finalMsg)
        }

        finalMsg = crypto.encipher(finalMsg, serverIp.port)
        println(finalMsg)

        socketChannel.use {
            val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            output.clear()
            output.put(finalMsg.toByteArray(Charsets.UTF_8))
            output.flip()    // reset the buffer position to forward data
            bytesWritten = socketChannel.write(output)
            output.clear()
        }
        return bytesWritten
    } finally {
        socketChannel.close()
    }
}

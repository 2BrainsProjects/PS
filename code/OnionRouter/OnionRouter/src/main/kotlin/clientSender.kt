import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(InetSocketAddress(8080), "password", "hello", listOf("127.0.0.1:8082", "127.0.0.1:8081"))
}

fun clientSender(
    serverPort: InetSocketAddress,
    pwd: String,
    msg: String,
    nodes: List<String>,
    certificatePath: String = System.getProperty("user.dir") + "\\crypto"
): Int{
    println("started")
    val socketChannel = SocketChannel.open(serverPort)
    var bytesWritten: Int

    try {
        val crypto = Crypto()
        val ip = socketChannel.socket().localAddress.toString()
        crypto.generateClientCSR(socketChannel.socket().port, ip, pwd)

        var finalMsg = msg

        nodes.reversed().forEach{
            val port = it.split(":")[1].toInt()
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$it"
        }

        finalMsg = crypto.encipher(finalMsg, serverPort.port, certificatePath)

        socketChannel.use {
            val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            output.clear()
            output.put(finalMsg.toByteArray(Charsets.UTF_8))
            output.flip()    // reset the buffer position to forward data
            socketChannel.write(output)
            output.clear()
        }
    } finally {
        socketChannel.close()
    }
}

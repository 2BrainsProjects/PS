import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

fun main() {
    clientSender(InetSocketAddress("127.0.0.1",8081), "password", "hello", listOf("127.0.0.1:8082"))
}

/**
 * Method to create a client sender where it creates a socket to send a message.
 * @param serverIp - InetSocketAddress of the client sender
 * @param pwd - password of the client sender CSR
 * @param msg - message to send
 * @param nodes - list of nodes the message needs to pass through
 * @param certificatePath - path where the certificate will be saved
 * @return how many bytes have sent
 */
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

        // generate the CSR to register in the API as a client
        val csrOutput = crypto.generateClientCSR(serverIp.port, serverIp.toString(), pwd)
        println("message: $msg")

        var finalMsg = msg

        // reverse the nodes list to facilitate the user, so he just have to build the message path in order
        nodes.reversed().forEach{
            val port = it.split(":")[1].toInt()
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$it"
            println(finalMsg)
        }

        finalMsg = crypto.encipher(finalMsg, serverIp.port)
        println(finalMsg)

        // writing to socket
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

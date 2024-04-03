import java.io.File
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

fun main(){

    println("started")
    val serverPort = 8080
    val socketChannel = SocketChannel.open(InetSocketAddress(serverPort))

    try {
        val crypto = Crypto()
        val ip = socketChannel.socket().localAddress.toString()
        val pwd = "password"
        crypto.generateClientCSR(socketChannel.socket().port, ip, pwd)

        val msg = "hello"
        val nodes = listOf("127.0.0.1:8081", "127.0.0.1:8082")
        var finalMsg = msg

        nodes.reversed().forEach{
            val port = it.split(":")[1].toInt()
            finalMsg = crypto.encipher(finalMsg, port)
            finalMsg += "||$it"
        }

        finalMsg = crypto.encipher(finalMsg, serverPort)

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

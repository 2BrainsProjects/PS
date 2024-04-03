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
    val socketChannel = SocketChannel.open(InetSocketAddress(8080))
    val crypto = Crypto()
    val ip = socketChannel.socket().localAddress.toString()
    val pwd = "password"
    crypto.generateClientCSR(socketChannel.socket().port, ip, pwd)

    val msg = "hello"
    val nodes = listOf("127.0.0.1:8082")

    /*
    encriptar a msg
    adiconar ip
    e.g. ...Enc8082("hello")||127.0.0.1:8082..
    */
    val finalMsg = msg + "||" + nodes.joinToString("||")

    val encMsg = crypto.encipher(finalMsg).toByteArray(Charsets.UTF_8)
    /*
    iterar sobre todos os ips para formar as várias camadas de encriptação
     */
    socketChannel.use {
        val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        output.clear()
        output.put(encMsg)
        output.flip()    // reset the buffer position to forward data
        socketChannel.write(output)
        output.clear()
    }
}

// u1 -> on1 -> u2
// u1 -> on1 -> on2

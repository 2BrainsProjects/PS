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
    val finalMsg = msg + "||" + nodes.joinToString("||")

    // simulating API request

//    file.readLines().joinToString("\n").toByteArray(Charsets.UTF_8)
//    println(file.readLines().joinToString("\n"))

    // USAR JCA PARA GERAR AS CHAVES
    // erro ao usar uma chave gerada pelo openssl para encriptar com JCA
    // criar CSR com a chave privada gerada: openssl req -key a.key -new -out a.csr

//    // [B@2a3046da
//    val keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair()
//    val private = keyPair.private.encoded
//    val public = keyPair.public
//    println(private)
    //val publicKey = KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(file.readBytes()))
    //cipher.init(Cipher.ENCRYPT_MODE, publicKey)
    val encMsg = crypto.encryptMessage(8080, finalMsg)

    /*
    private fun signedCertificateCommand(clientId: Int, path: String, pathServer: String): String =
        "openssl x509 -req -days 365 -in $path/$clientId.csr -CA $pathServer/certificate.crt -CAkey $pathServer/privateKey.key -out $path/$clientId.crt"

     */
    socketChannel.use {
        val output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        output.clear()
        output.put(encMsg.toByteArray(Charsets.UTF_8))
        output.flip()    // reset the buffer position to forward data
        socketChannel.write(output)
        output.clear()
    }
}

// u1 -> on1 -> u2
// u1 -> on1 -> on2

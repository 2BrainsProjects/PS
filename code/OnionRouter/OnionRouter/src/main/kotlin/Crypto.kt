import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class Crypto {
    val cipher = Cipher.getInstance("RSA")
    val keyFactory = KeyFactory.getInstance("RSA")
    fun generateClientCSR(port: Int, ip: String, pwd: String, basePath: String = path): List<String> {
        generateKeys(port, basePath)
        answeringCSRCreation(port, ip, pwd)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    private fun answeringCSRCreation(port: Int, ip: String, password: String, basePath: String = path) {
        val command =
            "openssl req -out $basePath/$port.csr -key $basePath/priv$port.pem -new"
        try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()

            // Provide input to the process
            val writer = BufferedWriter(OutputStreamWriter(process.outputStream))
            repeat(5) {
                writer.write("\n")
                writer.flush()
            }
            writer.write("$ip\n")
            writer.flush()
            writer.write("\n")
            writer.flush()
            writer.write("$password\n")
            writer.flush()
            writer.write("\n")
            writer.flush()
            writer.close()

            process.waitFor()
        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }
    }

    private fun generateKeys(port: Int, basePath: String = path){
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private.encoded
        val file = File("$basePath/priv$port.pem")
        file.createNewFile()
        file.writeBytes(privateKey)

        val publicKey = keyPair.public.encoded
        val file2 = File("$basePath/pub$port.pem")
        file2.createNewFile()
        file2.writeBytes(publicKey)
    }

    fun decryptMessage(port: Int, encMsg: String , basePath: String = path): String {
        println("entered decryptMessage")
        val privateKey = keyFactory.generatePrivate(PKCS8EncodedKeySpec(File("$basePath\\priv$port.pem").readBytes()))
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        var resultByteArray = ByteArray(0)
        var encMsgByteArray = encMsg.toByteArray(Charsets.UTF_8)
        while(encMsgByteArray.size >= 256){
            val window = encMsgByteArray.take(255).toByteArray()
            resultByteArray += cipher.update(window)
            encMsgByteArray = encMsgByteArray.drop(255).toByteArray()
        }
        val decrypted = cipher.doFinal(encMsgByteArray)
        resultByteArray += decrypted

        return String(resultByteArray, Charsets.UTF_8)
    }

    fun encryptMessage(port: Int, msg: String , basePath: String = path): ByteArray {
        println("entered encryptMessage")
        val publicKey = keyFactory.generatePublic(X509EncodedKeySpec(File("$basePath\\pub$port.pem").readBytes()))
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encrypted = cipher.doFinal(msg.toByteArray(Charsets.UTF_8))

        return encrypted
    }

    private fun runCommand(command: String){
        try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }
    }

    companion object{
        private val path
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\crypto"
    }
}
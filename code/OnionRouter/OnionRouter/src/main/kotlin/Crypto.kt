import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class Crypto {
    fun generateClientCSR(port: Int, ip: String, pwd: String, basePath: String = path): List<String> {
        answeringCSRCreation(port, ip, pwd)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    private fun answeringCSRCreation(port: Int, ip: String, password: String, basePath: String = path) {
        val command =
            //"openssl req -out $basePath/$port.csr -new -newkey rsa:2048 -nodes -keyout $basePath/$port.key"
            "openssl req -key $basePath/$port.key -new -out $basePath/$port.csr"
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

    fun tempGeneratePubKey(port: Int, basePath: String = path){
        val command = "openssl rsa -pubout -in $basePath/$port.key -out $basePath/$port.pub"
        try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }
    }

    fun decryptMessage(port: Int, msg:String , basePath: String = path): String {
        val keyPath = "$basePath\\$port.key"
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
        return plaintext
    }

    companion object{
        private val path
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\crypto"
    }
}
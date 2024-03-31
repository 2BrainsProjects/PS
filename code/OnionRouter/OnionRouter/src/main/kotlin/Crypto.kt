import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher

class Crypto {
    fun generateClientCSR(port: Int, ip: String, pwd: String, basePath: String = path): List<String> {
        generatePrivateKey(port, basePath)
        answeringCSRCreation(port, ip, pwd)
        generatePublicKey(port, basePath)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    private fun answeringCSRCreation(port: Int, ip: String, password: String, basePath: String = path) {
        val command =
            "openssl req -out CSR.csr -key $basePath/priv$port.pem -new"
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

    private fun generatePrivateKey(port: Int, basePath: String = path){
        val command = "openssl genrsa -out $basePath/priv$port.pem 2048"
        runCommand(command)
    }

    private fun generatePublicKey(port: Int, basePath: String = path){
        val command = "openssl rsa -pubout -in $basePath/priv$port.pem -out $basePath/pub$port.pem"
        runCommand(command)
    }

    fun decryptMessage(port: Int, encMsg: String , basePath: String = path): String {
        println("entered decryptMessage")
        val file = File("$basePath\\$port.enc")
        file.createNewFile()
        file.writeText(encMsg)
        println("encMsg: $encMsg")
        val command =
                "openssl rsautl -decrypt -inkey $basePath\\priv$port.pem -in $basePath\\$port.enc > $basePath\\$port.txt"
        runCommand(command)
        val tempFile = File("$basePath\\$port.txt")
        val msg = tempFile.readText()
        println("msg: $msg")
        //tempFile.delete()
        //file.delete()
        return msg
    }

    fun encryptMessage(port: Int, msg: String , basePath: String = path): String {
        println("entered encryptMessage")
        val file = File("$basePath\\$port.txt")
        file.createNewFile()
        file.writeText(msg)
        println("msg: $msg")
        val command =
            "openssl rsautl -encrypt -inkey $basePath\\pub$port.pem -pubin -in $basePath\\$port.txt -out $basePath\\$port.enc"
        runCommand(command)
        val tempFile = File("$basePath\\$port.enc")
        val encMsg = tempFile.readText()
        println("encMsg: $encMsg")
        //tempFile.delete()
        //file.delete()
        return encMsg
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
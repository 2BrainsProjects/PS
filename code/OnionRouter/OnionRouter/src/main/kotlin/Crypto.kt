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
        answeringCSRCreation(port, ip, pwd)
        generatePubKey(port, basePath)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    private fun answeringCSRCreation(port: Int, ip: String, password: String, basePath: String = path) {
        val command =
            "openssl req -out $basePath/$port.csr -new -newkey rsa:2048 -nodes -keyout $basePath/$port.key"
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

    fun generatePubKey(port: Int, basePath: String = path){
        val command = "openssl rsa -pubout -in $basePath/$port.key -out $basePath/$port.pub"
        runCommand(command)
    }

    fun decryptMessage(port: Int, encMsg: String , basePath: String = path): String {
        println("entered decryptMessage")
        val file = File("$basePath\\tempdec$port.enc")
        file.createNewFile()
        file.writeText(encMsg)
        println("encMsg: $encMsg")
        // problema está aqui
        val command =
                "openssl rsautl -decrypt -inkey $basePath\\$port.key -in $basePath\\tempdec$port.txt > $basePath\\temp$port.txt"
        runCommand(command)
        val tempFile = File("$basePath\\temp$port.txt")
        val msg = tempFile.readText()
        println("msg: $msg")
        //tempFile.delete()
        //file.delete()
        return msg
    }

    fun encryptMessage(port: Int, msg: String , basePath: String = path): String {
        println("entered encryptMessage")
        val file = File("$basePath\\tempenc$port.txt")
        file.createNewFile()
        file.writeText(msg)
        println("msg: $msg")
        val command =
            "openssl rsautl -encrypt -inkey $basePath\\$port.pub -pubin -in $basePath\\tempenc$port.txt > $basePath\\tempdec$port.enc"
        runCommand(command)
        val tempFile = File("$basePath\\tempdec$port.enc")
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
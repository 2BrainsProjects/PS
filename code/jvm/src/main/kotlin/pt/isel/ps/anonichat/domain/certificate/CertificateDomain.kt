package pt.isel.ps.anonichat.domain.certificate

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Component
class CertificateDomain {

    /**
     * this function requires openssl installed in system's PATH.
     * the resultant certificate will be in clientId.crt
     * @param clientId user's id
     * @param name user's name
     * @param email user's email
     * @param password user's password
     */
    fun createCertCommand(clientCSR: String, clientId: Int, name: String, email: String, password: String, path:String): String {
        createCSRTempFile(clientId, clientCSR, path)
        println("csr temp file created")

        val certFile = File("$path/$clientId.crt")
        certFile.createNewFile()

        val signedCertificateCommand = signedCertificateCommand(clientId, path, SERVER_PATH)
        execute(signedCertificateCommand)

        val crtContent = readFile("$path/$clientId.crt")
        deleteTempFile("$path/$clientId.csr")
        return crtContent
    }

    private fun execute(command: String) {
        try {
            val runtime = Runtime.getRuntime()
            runtime.exec(command)

        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }
    }

    fun readFile(filePath: String): String {
        val bufferedReader = BufferedReader(FileInputStream(filePath).bufferedReader())
        val text = bufferedReader.readText()
        bufferedReader.close()
        return text
    }

    private fun deleteTempFile(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun createCSRTempFile(clientId: Int, clientCSR: String, path: String) {
        val dir = File(path)
        if(!dir.exists()) {
            execute("mkdir $path")
        }
        val file = File("$path/$clientId.csr")
        if (!file.exists()) {
            execute("cmd /c type nul > $path/$clientId.csr")
            BufferedWriter(OutputStreamWriter(FileOutputStream("$path/$clientId.csr"))).use {
                it.write("-----BEGIN CERTIFICATE REQUEST-----\n")
                it.write(clientCSR + "\n")
                it.write("-----END CERTIFICATE REQUEST-----\n")
            }
        }
    }

    private fun signedCertificateCommand(clientId: Int, path: String, pathServer: String): String =
        "openssl x509 -req -days 365 -in $path/$clientId.csr -CA $pathServer/certificate.crt -CAkey $pathServer/privateKey.key -out $path/$clientId.crt"

    companion object{
        private val SERVER_PATH
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\certificates"
    }
}

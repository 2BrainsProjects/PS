package pt.isel.ps.anonichat.domain.certificate

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter

@Component
class CertificateDomain  {

    /**
     * this function requires openssl installed in system's PATH.
     * the resultant certificate will be in clientId.crt
     * @param clientCSR user's certificate signing request
     * @param clientId user's id
     * @param path path of the certificate
     * @return the path of the certificate
     */
    fun createCertCommand(
        clientCSR: String,
        clientId: Int,
        path: String
    ): String {
        val csrFile = createCSRTempFile(clientId, clientCSR, path)
        val certFile = File("$path/$clientId.cer")
        if(certFile.exists()) certFile.delete()
        certFile.createNewFile()

        val signedCertificateCommand = signedCertificateCommand(clientId, path)
        execute(signedCertificateCommand)

        // wait for the file to be written on
        while(BufferedReader(FileInputStream(certFile).bufferedReader()).readLines().isEmpty()){}
        csrFile.delete()
        return "$path/$clientId.cer"
    }

    private fun execute(command: String) {
        try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()

            process.waitFor()
        }catch (e: Exception){
            throw IllegalStateException(e.message)
        }
    }

    fun readFile(filePath: String): String {
        val bufferedReader = BufferedReader(FileInputStream(filePath).bufferedReader())
        val text = bufferedReader.readText()
        bufferedReader.close()
        return text
    }

    private fun createCSRTempFile(clientId: Int, clientCSR: String, path: String): File {
        val dir = File(path)
        if(!dir.exists()) {
            dir.mkdir()
        }
        val file = File("$path/$clientId.csr")
        if (!file.exists()) {
            file.createNewFile()
            BufferedWriter(OutputStreamWriter(FileOutputStream("$path/$clientId.csr"))).use {
                it.write(clientCSR)
            }
        }
        return file
    }

    private fun signedCertificateCommand(clientId: Int, path: String): String =
        "openssl x509 -req -days 365 -in $path/$clientId.csr -CA $SERVER_PATH/certificate.crt -CAkey $SERVER_PATH/privateKey.key -out $path/$clientId.cer"

    companion object{
        private val SERVER_PATH
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\certificates"
    }
}

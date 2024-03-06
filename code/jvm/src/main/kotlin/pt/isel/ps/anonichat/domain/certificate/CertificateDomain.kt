package pt.isel.ps.anonichat.domain.certificate

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

@Component
class CertificateDomain {

    fun loadServerCertificate(): X509Certificate {
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certStream = FileInputStream("certificate/certificate.crt")
            return certificateFactory.generateCertificate(certStream) as X509Certificate
        } catch (e: Exception) {
            throw IllegalStateException("failed to load server certificate")
        }
    }

    fun certificateFromString(base64: ByteArray): X509Certificate {
        val inputStream = ByteArrayInputStream(base64)

        return CertificateFactory.getInstance("X.509").generateCertificate(inputStream) as X509Certificate
    }

    /**
     * this function requires openssl installed in system's PATH.
     * the resultant certificate will be in clientId.crt
     * @param clientId user's id
     * @param name user's name
     * @param email user's email
     * @param password user's password
     */
    fun createKeyCommand(publicKey: String, clientId: Int, name: String, email: String, password: String): String {
        val createKeyCommand = "openssl genrsa -out $BASE_PATH$clientId.key 1024"
        execute(createKeyCommand)

        val createCSRCommand = createCSRCommand(clientId)
        execute(createCSRCommand)
        answeringCSRCreation(name, email, password)

        val signedCertificateCommand = signedCertificateCommand(clientId)
        execute(signedCertificateCommand)

        val crtContent = readFile("$BASE_PATH$clientId.crt")
        return crtContent
    }

    private fun execute(command: String) {
        try {
            val processBuilder = ProcessBuilder(command.split(" "))
            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                println(line)
            }

            process.waitFor()
        } catch (e: Exception) {
            throw IllegalStateException("Couldnt perform the command: $command")
        }
    }

    private fun readFile(filePath: String): String {
        val bufferedReader = BufferedReader(FileInputStream(filePath).bufferedReader())
        val text = bufferedReader.readText()
        bufferedReader.close()
        return text
    }

    private fun answeringCSRCreation(name: String, email: String, password: String) {
        // Country Name(2 letter code):
        execute("")
        // State or Province Name (full name):
        execute("")
        // Locality Name (eg, city):
        execute("")
        // Organization Name (eg, company):
        execute("")
        // Organizational Unit Name (eg, section):
        execute("")
        // Common Name(e.g. server FQDN or YOUR name): user's username
        execute(name)
        // Email Address: user's email
        execute(email)
        // A challenge password: (1234567890!Aa) user's password
        execute(password)
        // An optional company name:
        execute("")
    }

    private fun createCSRCommand(clientId: Int) =
        //                            publicKey
        "openssl req -new -key $BASE_PATH$clientId.key -out $BASE_PATH$clientId.csr"

    private fun signedCertificateCommand(clientId: Int) =
        "openssl x509 -req -days 365 -in $BASE_PATH$clientId.csr -CA $BASE_PATH/certificate.crt -CAkey " +
            "$BASE_PATH/privateKey.key -set_serial 01 -out $BASE_PATH$clientId.crt"

    companion object {
        const val BASE_PATH = "/certificate"
    }
}

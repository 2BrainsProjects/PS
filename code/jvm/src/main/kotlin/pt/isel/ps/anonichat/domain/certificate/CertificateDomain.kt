package pt.isel.ps.anonichat.domain.certificate

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
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
        val createCSRCommand = createCSRCommand(clientId, publicKey)
        execute(createCSRCommand)
        answeringCSRCreation(name, email, password)

        val signedCertificateCommand = signedCertificateCommand(clientId)
        execute(signedCertificateCommand)

        val crtContent = readFile("$BASE_PATH/$clientId.crt")
        return crtContent
    }

    private fun execute(command: String) {
        println(command)

        try {
            val runtime = Runtime.getRuntime()
            runtime.exec("cmd /c $command")

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

    private fun createCSRCommand(clientId: Int, clientPublicKey: String): String {
        val dir = File(BASE_PATH)
        if(!dir.exists()) {
            execute("mkdir $BASE_PATH")
        }
        val file = File("$BASE_PATH$USERS/$clientId.key")
        if (!file.exists()) {
            execute("echo $clientPublicKey > $BASE_PATH$USERS/$clientId.key")
        }
        // -key pede uma chave privada
        return "openssl req -new -key $BASE_PATH$USERS/$clientId.key -out $BASE_PATH$USERS/$clientId.csr"
    }

    private fun signedCertificateCommand(clientId: Int): String{
        val file = File("$BASE_PATH$USERS/$clientId.crt")
        if (!file.exists()) {
            execute("type nul > $BASE_PATH$USERS/$clientId.crt")
        }
        return "openssl x509 -req -days 365 -in $BASE_PATH$USERS/$clientId.csr -CA $BASE_PATH/certificate.crt -CAkey " +
                "$BASE_PATH/privateKey.key -out $BASE_PATH$USERS/$clientId.crt"
    }

    companion object {
        const val BASE_PATH = "D:\\ISEL\\6sem\\PS\\code\\jvm\\certificates"
        const val USERS = "/users"
    }
}

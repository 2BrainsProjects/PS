package pt.isel.ps.anonichat.domain.certificate

import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.FileInputStream
import java.io.InputStreamReader
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

@Component
class CertificateDomain {

    fun createClientCertificate(publicKey: String): X509Certificate {
        val serverKeyPair = loadServerKeyPair()

        val clientPublicKey = parsePublicKey(publicKey)
        val signatureBytes = createClientCertificate(clientPublicKey, serverKeyPair.private)

        val clientCertificate = certificateFromString(signatureBytes)

        return clientCertificate
    }

    fun loadServerCertificate(): X509Certificate {
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certStream = FileInputStream("certificate/certificate.crt")
            return certificateFactory.generateCertificate(certStream) as X509Certificate
        } catch (e: Exception) {
            throw IllegalStateException("failed to load server certificate")
        }
    }

    private fun loadServerKeyPair(): KeyPair {
        try {
            val keystoreFile = FileInputStream("src/main/resources/keystore.p12")

            val keystore = KeyStore.getInstance("PKCS12")
            keystore.load(keystoreFile, "".toCharArray())

            val keyAlias = keystore.aliases().nextElement()
            val privateKey = keystore.getKey(keyAlias, "".toCharArray()) as PrivateKey
            val cert = keystore.getCertificate(keyAlias) as X509Certificate

            return KeyPair(cert.publicKey, privateKey)
        } catch (e: Exception) {
            throw IllegalStateException("failed to load server keypair")
        }
    }

    private fun parsePublicKey(publicKeyBase64: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(publicKeyBase64)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(keyBytes)
        return keyFactory.generatePublic(keySpec)
    }

    private fun createClientCertificate(clientPublicKey: PublicKey, serverPrivateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256WithRSA")
        signature.initSign(serverPrivateKey)
        signature.update(clientPublicKey.encoded)
        return signature.sign()
    }

    private fun certificateFromString(base64: ByteArray): X509Certificate {
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
    fun createKeyCommand(publicKey: String, clientId: Int, name: String, email: String, password: String){
        val createKeyCommand = "openssl genrsa -out $BASE_PATH${clientId}.key 1024"
        execute(createKeyCommand)
        //                                                  publicKey
        val createCSRCommand = "openssl req -new -key $BASE_PATH${clientId}.key -out $BASE_PATH${clientId}.csr"
        execute(createCSRCommand)
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

        val signedCertificateCommand = "openssl x509 -req -days 365 -in $BASE_PATH${clientId}.csr -CA " +
                "$BASE_PATH/certificate.crt -CAkey $BASE_PATH/privateKey.key -set_serial 01 -out $BASE_PATH${clientId}.crt"
        execute(signedCertificateCommand)
    }

    private fun execute(command: String){
        try {
            val processBuilder = ProcessBuilder(command.split(" "))
            val process = processBuilder.start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = null

            while (reader.readLine().also { line = it } != null) {
                println(line)
            }

            process.waitFor()
        } catch (e: Exception) {
            throw IllegalStateException("Couldnt perform the command: $command")
        }
    }

    companion object{
        const val BASE_PATH = "/certificate"
    }
}
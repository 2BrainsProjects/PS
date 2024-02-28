package pt.isel.daw.gomoku.domain.user

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.daw.gomoku.domain.user.utils.TokenEncoder
import java.io.FileInputStream
import java.security.cert.X509Certificate
import java.util.*
import java.security.*
import java.security.cert.CertificateFactory
import java.security.spec.X509EncodedKeySpec

/**
 * The domain of the users containing all the users logic
 */
@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UserDomainConfig
) {
    val maxTokensPerUser get() = config.maxTokensPerUser

    fun verifyPassword(password: String, hashedPassword: String) = passwordEncoder.matches(password, hashedPassword)

    fun encodePassword(password: String): String = passwordEncoder.encode(password)

    fun hashToken(token: String): String = tokenEncoder.hash(token)

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun verifyToken(token: String, tokenHash: String) = tokenEncoder.matches(token, tokenHash)

    fun isToken(token: String): Boolean = try {
        Base64.getUrlDecoder().decode(token).size == config.tokenSizeInBytes
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return minOf(absoluteExpiration, rollingExpiration)
    }

    fun hasTokenExpired(token: Token, clock: Clock): Boolean {
        val now = clock.now()
        val expirationTime = token.createdAt + config.tokenTtl
        val rollingExpirationTime = token.lastUsedAt + config.tokenRollingTtl
        return now.isAfter(expirationTime) || now.isAfter(rollingExpirationTime)
    }

    fun signPublicKey(publicKey: String, serverCertificate: X509Certificate): X509Certificate {
        val serverKeyPair = loadServerKeyPair() ?: throw IllegalStateException("failed to load server keypair")

        val clientPublicKey = parsePublicKey(publicKey)
        val signatureBytes = signPublicKey(clientPublicKey, serverKeyPair.private)

        val clientCertificate = buildClientCertificate(clientPublicKey, serverCertificate, signatureBytes)

        return clientCertificate
    }

    fun loadServerKeyPair(): KeyPair? {
        try {
            val keystoreFile = FileInputStream("src/main/resources/keystore.p12")

            val keystore = KeyStore.getInstance("PKCS12")
            keystore.load(keystoreFile, "".toCharArray())

            // Obtendo a chave privada do keystore
            val alias = keystore.aliases().nextElement()
            val privateKey = keystore.getKey(alias, "".toCharArray()) as PrivateKey

            // Obtendo o certificado correspondente à chave privada
            val cert = keystore.getCertificate(alias) as X509Certificate

            return KeyPair(cert.publicKey, privateKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun loadServerCertificate(): X509Certificate? {
        try {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certStream = FileInputStream("certificate/certificate.crt")
            return certificateFactory.generateCertificate(certStream) as X509Certificate
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun parsePublicKey(publicKeyBase64: String): PublicKey {
        val keyBytes = Base64.getDecoder().decode(publicKeyBase64)
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(keyBytes)
        return keyFactory.generatePublic(keySpec)
    }

    fun signPublicKey(clientPublicKey: PublicKey, serverPrivateKey: PrivateKey): ByteArray {
        val signature = Signature.getInstance("SHA256WithRSA")
        signature.initSign(serverPrivateKey)
        signature.update(clientPublicKey.encoded)
        return signature.sign()
    }

    fun buildClientCertificate(clientPublicKey: PublicKey, serverCertificate: X509Certificate, signatureBytes: ByteArray): X509Certificate {TODO()}

/*        try {
            val serialNumber = BigInteger(64, java.security.SecureRandom())

            val notBefore = Date()
            val notAfter = Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)

            val clientSubjectDN = X500Principal("CN=Client Certificate")

            val clientCertificateBuilder = X509CertificateBuilder(
                    serverCertificate.issuerX500Principal,
                    serialNumber,
                    notBefore,
                    notAfter,
                    clientSubjectDN,
                    SubjectPublicKeyInfo.getInstance(clientPublicKey.encoded)
            )

            clientCertificateBuilder.addExtension(
                    serverCertificate.getExtensionValue("1.3.6.1.5.5.7.1.3"),
                    true
            )

            val contentSigner = createContentSigner(serverCertificate.issuerX500Principal, serverCertificate.signatureAlgorithm, serverCertificate.privateKey)
            val clientCertificateHolder = clientCertificateBuilder.build(contentSigner)

            return CertificateFactory.getInstance("X.509")
                    .generateCertificate(clientCertificateHolder.encoded.inputStream()) as X509Certificate

        } catch (e: Exception) {
            e.printStackTrace()
        }
        throw IllegalStateException("Erro ao construir o certificado do cliente.")
    }

    fun createContentSigner(issuer: X500Principal, signatureAlgorithm: String, privateKey: PrivateKey): ContentSigner {
        return JcaContentSignerBuilder(signatureAlgorithm).build(privateKey)
    }*/

    private fun Instant.isAfter(instant: Instant) = toJavaInstant().isAfter(instant.toJavaInstant())

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 20
        const val MIN_EMAIL_LENGTH = 3
        const val MAX_EMAIL_LENGTH = 30
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 30
    }
}

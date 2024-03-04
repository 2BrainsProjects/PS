package pt.isel.ps.anonichat.domain.user.utils

import java.io.ByteArrayInputStream
import java.io.FileInputStream
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

//    fun buildClientCertificate(clientPublicKey: PublicKey, serverCertificate: X509Certificate, signatureBytes: ByteArray): X509Certificate {
//
//        try {
//            val serialNumber = BigInteger(64, java.security.SecureRandom())
//
//            val notBefore = Date()
//            val notAfter = Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)
//
//            val clientSubjectDN = X500Principal("CN=Client Certificate")
//
//            val clientCertificateBuilder = X509CertificateBuilder(
//                    serverCertificate.issuerX500Principal,
//                    serialNumber,
//                    notBefore,
//                    notAfter,
//                    clientSubjectDN,
//                    SubjectPublicKeyInfo.getInstance(clientPublicKey.encoded)
//            )
//
//            clientCertificateBuilder.addExtension(
//                    serverCertificate.getExtensionValue("1.3.6.1.5.5.7.1.3"),
//                    true
//            )
//
//            val contentSigner = createContentSigner(serverCertificate.issuerX500Principal, serverCertificate.signatureAlgorithm, serverCertificate.privateKey)
//            val clientCertificateHolder = clientCertificateBuilder.build(contentSigner)
//
//            return CertificateFactory.getInstance("X.509")
//                    .generateCertificate(clientCertificateHolder.encoded.inputStream()) as X509Certificate
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        throw IllegalStateException("Erro ao construir o certificado do cliente.")
//    }
//
//    fun createContentSigner(issuer: X500Principal, signatureAlgorithm: String, privateKey: PrivateKey): ContentSigner {
//        return JcaContentSignerBuilder(signatureAlgorithm).build(privateKey)
//    }
}
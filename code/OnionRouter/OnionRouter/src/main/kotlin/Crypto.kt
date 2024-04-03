import java.io.*
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Crypto {
    val sCipher = Cipher.getInstance("AES/GCM/NoPadding")
    val aCipher = Cipher.getInstance("RSA")
    val keyFactory = KeyFactory.getInstance("RSA")

    private val MARK_SIZE = 128
    private val KEY_SIZE = 256
    private val PASSWORD = "changeit" // do not change it
    private val JWE_HEADER = "{\"alg\":\"RSA\",\"enc\":\"AES/GCM/NoPadding\"}"

    fun generateClientCSR(port: Int, ip: String, pwd: String, basePath: String = path): List<String> {
        generateKeys(port, basePath)
        answeringCSRCreation(port, ip, pwd)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    private fun answeringCSRCreation(port: Int, ip: String, password: String, basePath: String = path) {
        val command =
            "openssl req -out $basePath/$port.csr -key $basePath/priv$port.pem -new"
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

    private fun generateKeys(port: Int, basePath: String = path){
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private.encoded
        val file = File("$basePath/priv$port.pem")
        file.createNewFile()
        file.writeBytes(privateKey)

        val publicKey = keyPair.public.encoded
        val file2 = File("$basePath/pub$port.pem")
        file2.createNewFile()
        file2.writeBytes(publicKey)
    }

     fun encipher(plain: String, port:Int, certificatePaths: List<String> = List(1){ path }):String {
        try {
            val pubKeyBytes = File("$path\\pub$port.pem").readBytes()
            val keySpec = X509EncodedKeySpec(pubKeyBytes)
            val publicKey = keyFactory.generatePublic(keySpec)
            //val publicKey = getPublicKeyFromCertificate(certificatePaths)

            val keyGenerator = KeyGenerator.getInstance(ALG_SYMMETRIC)
            keyGenerator.init(KEY_SIZE)
            val symmetricKey = keyGenerator.generateKey()
            val iv = IvParameterSpec(PASSWORD.toByteArray())
            return encipherString(plain, JWE_HEADER, symmetricKey, publicKey, iv, MARK_SIZE)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun getPublicKeyFromCertificate(certificatePaths: List<String> = List(1){ path }): PublicKey {
        try {
            val certList: ArrayList<X509Certificate> = ArrayList()
            var rootCertIndex = -1
            for (i in certificatePaths.indices) {
                val certificatePath = getCertificatePath(certificatePaths[i])
                if (certificatePaths[i].contains("trust-anchors")) {
                    rootCertIndex = i
                }
                val factory = CertificateFactory.getInstance(STD_CERTIFICATE)
                val fis = FileInputStream(certificatePath)
                val cert: X509Certificate = factory.generateCertificate(fis) as X509Certificate
                fis.close()
                certList.add(cert)
            }
            require(rootCertIndex != -1) { "No root certificate found in certificate chain" }

            val factory = CertificateFactory.getInstance(STD_CERTIFICATE)
            val certPath = factory.generateCertPath(certList)

            val trustAnchorSet: MutableSet<TrustAnchor> = HashSet()
            trustAnchorSet.add(TrustAnchor(certList[rootCertIndex], null))

            val params: CertPathParameters = PKIXParameters(trustAnchorSet)
            (params as PKIXParameters).isRevocationEnabled = false

            // performs the certificate validation
            val certPathValidator = CertPathValidator.getInstance("PKIX")
            certPathValidator.validate(certPath, params)
            return certList[0].publicKey
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun getPrivateKey(filePath: String = path): PrivateKey {
        val privKeyBytes = File(filePath).readBytes()
        val keySpec = PKCS8EncodedKeySpec(privKeyBytes)
        return keyFactory.generatePrivate(keySpec)
    }

    @Throws(Exception::class)
    private fun encipherString(
        strToCypher: String,
        aditionalData: String,
        symmetricKey: SecretKey,
        publicKey: PublicKey,
        iv: IvParameterSpec,
        markSize: Int
    ):String {

        val gcmParameterSpec = GCMParameterSpec(markSize, iv.iv)

        sCipher.init(Cipher.ENCRYPT_MODE, symmetricKey, gcmParameterSpec)

        val encrypted = sCipher.doFinal(strToCypher.toByteArray())

        val encryptedByteMsg = encrypted.copyOf(encrypted.size - (markSize / 8))

        val mark = Arrays.copyOfRange(encrypted, encrypted.size - (markSize / 8), encrypted.size)

        val encryptedMsg: String = Base64.getEncoder().encodeToString(encryptedByteMsg)

        aCipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedKey = aCipher.doFinal(symmetricKey.encoded)
        val encryptedKeyStr: String = Base64.getEncoder().encodeToString(encryptedKey)

        val header: String = Base64.getEncoder().encodeToString(aditionalData.toByteArray())

        val markStr: String = Base64.getEncoder().encodeToString(mark)
        val ivStr: String = Base64.getEncoder().encodeToString(iv.iv)

        val jweToken = String.format("%s.%s.%s.%s.%s", header, encryptedKeyStr, ivStr, encryptedMsg, markStr)
        return jweToken
    }

    private fun getCertificatePath(certificateName: String, certificatePath: String = path): String {
        return "$certificatePath/$certificateName"
    }

    @Throws(Exception::class)
    fun decipher(cipheredText: String, port: Int, keyPath: String = path):String {

        val parts = cipheredText.split(".").map { s -> Base64.getDecoder().decode(s) }

        val header = parts[0]
        val encryptedKeyStr = parts[1]
        val ivStr = parts[2]
        val encryptedMsg = parts[3]
        val markStr = parts[4]

        val headerParts = String(header).split(",\"").toMutableList()

        headerParts[0] = headerParts[0].substring(2)
        val headerArgs = emptyList<String>().toMutableList()
        for (i in headerParts.indices) {
            headerArgs.add(headerParts[i].split("\"")[2])
        }
        var toReturn = ""
        try {
            val privateKey = getPrivateKey("$keyPath\\priv$port.pem")
            val keyCipher = Cipher.getInstance(headerArgs[0])

            keyCipher.init(Cipher.DECRYPT_MODE, privateKey)

            val decipherKey = keyCipher.doFinal(encryptedKeyStr)
            val symmetricKey: SecretKey = SecretKeySpec(decipherKey, 0, decipherKey.size, ALG_SYMMETRIC)

            val textCipher = Cipher.getInstance(headerArgs[1])
            val gcmParameterSpec = GCMParameterSpec(markStr.size * 8, ivStr)

            textCipher.init(Cipher.DECRYPT_MODE, symmetricKey, gcmParameterSpec)

            val outputStream = ByteArrayOutputStream()
            outputStream.write(encryptedMsg)
            outputStream.write(markStr)

            val decipherText = outputStream.toByteArray()

            // nas teoricas ver uma regra do perfil PKIX que verifica se existe mais do que 1 certificado folha na cadeia
            val decipheredText = textCipher.doFinal(decipherText)
            toReturn = (String(decipheredText))
        } catch (e: Exception) {
            println(e.message)
        }
        return toReturn
    }


    companion object{
        private val path
            get() = path()
        private fun path() = System.getProperty("user.dir") + "\\crypto"
    }
}
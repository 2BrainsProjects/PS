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

class Crypto(private val basePath: String = System.getProperty("user.dir") + "\\crypto") {

    private val sCipher = Cipher.getInstance("AES/GCM/NoPadding")
    private val aCipher = Cipher.getInstance(ALG_ASYMMETRIC)
    private val keyFactory = KeyFactory.getInstance(ALG_ASYMMETRIC)

    /**
     * Method to generate the CSR for the client.
     * @param port - port of the client
     * @param ip - ip of the client
     * @param pwd - password of the client
     * @return the CSR
     */
    fun generateClientCSR(port: Int, cn: String, pwd: String): List<String> {
        generateKeys(port)
        answeringCSRCreation(port, cn, pwd)
        BufferedReader(InputStreamReader(FileInputStream("$basePath/$port.csr"))).use {
            return it.readLines().drop(1).dropLast(1)
        }
    }

    /**
     * Method to anwser the enumerous questions of the CSR creation.
     * @param port - port of the host
     * @param cn - common name of the host
     * @param password - password of the host
     */
    private fun answeringCSRCreation(port: Int, cn: String, password: String) {
        val filePath = "$basePath/$port.csr"
        File(filePath).createNewFile()

        val command =
            "openssl req -out $filePath -key $basePath/priv$port.pem -new"
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
            listOf("$cn\n", "\n", "$password\n", "\n").forEach {
                writer.write(it)
                writer.flush()
            }
            writer.close()

            process.waitFor()

        } catch (e: Exception) {
            throw IllegalStateException(e.message)
        }
    }

    /**
     * Method to generate the key pair.
     * @param port - port of the host
     */
    fun generateKeys(port: Int){
        val keyPairGenerator = KeyPairGenerator.getInstance(ALG_ASYMMETRIC)
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        val privateKey = keyPair.private.encoded
        createAndWriteFile(privateKey, "$basePath/priv$port.pem")

        val publicKey = keyPair.public.encoded
        createAndWriteFile(publicKey, "$basePath/pub$port.pem")
    }

    /**
     * Method to create a file and write a key in it.
     * @param key - key to write in the file
     * @param filePath - path of the file
     */
    private fun createAndWriteFile(key: ByteArray, filePath: String){
        val file2 = File(filePath)
        file2.createNewFile()
        file2.writeBytes(key)
    }

    /**
     * Method to encipher a message using hybrid mode.
     * @param plain - message to encipher
     * @param port - port of the host to encipher the message
     * @return the enciphered message
     */
    fun encipher(plain: String, port:Int):String {
        try {
            val pubKeyBytes = File("$basePath\\pub$port.pem").readBytes()
            val keySpec = X509EncodedKeySpec(pubKeyBytes)
            val publicKey = keyFactory.generatePublic(keySpec)
            //val publicKey = getPublicKeyFromCertificate(basePath)

            val keyGenerator = KeyGenerator.getInstance(ALG_SYMMETRIC)
            keyGenerator.init(KEY_SIZE)
            val symmetricKey = keyGenerator.generateKey()
            val iv = IvParameterSpec(PASSWORD.toByteArray())
            return encipherString(plain, JWE_HEADER, symmetricKey, publicKey, iv, MARK_SIZE)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    /**
     * Method to get the public key from a certificate.
     * @param certificatePath - path of the certificate
     * @return the public key
     */
    private fun getPublicKeyFromCertificate(certificatePath: String): PublicKey {
        val factory = CertificateFactory.getInstance(STD_CERTIFICATE)
        val fis = FileInputStream(certificatePath)
        val cert: X509Certificate = factory.generateCertificate(fis) as X509Certificate
        fis.close()
        return cert.publicKey
    }

    /**
     * Method to get the private key from a file.
     * @param filePath - path of the file
     * @return the private key
     */
    private fun getPrivateKey(filePath: String): PrivateKey {
        val privKeyBytes = File(filePath).readBytes()
        val keySpec = PKCS8EncodedKeySpec(privKeyBytes)
        return keyFactory.generatePrivate(keySpec)
    }

    /**
     * Method to encipher a message using hybrid mode and JWE.
     * @param strToCypher - message to encipher
     * @param aditionalData - additional data to add to the header
     * @param symmetricKey - symmetric key to encipher the message
     * @param publicKey - public key to encipher the symmetric key
     * @param iv - initialization vector
     * @param markSize - size of the mark
     * @return the enciphered message
     */
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

    /**
     * Method to get the path of a certificate.
     * @param certificateName - name of the certificate
     * @param certificatePath - path of the certificate
     * @return the path of the certificate
     */
    private fun getCertificatePath(certificateName: String, certificatePath: String): String {
        return "$certificatePath/$certificateName"
    }

    /**
     * Method to decipher a message using hybrid mode.
     * @param cipheredText - text to decipher
     * @param port - port of the host to decipher the message
     * @return the deciphered message
     */
    @Throws(Exception::class)
    fun decipher(cipheredText: String, port: Int):String {

        val parts = cipheredText.split(".").map { s -> Base64.getDecoder().decode(s) }

        val (header, encryptedKeyStr, ivStr, encryptedMsg, markStr) = parts

        val headerParts = String(header).split(",\"").toMutableList()

        headerParts[0] = headerParts[0].substring(2)
        val headerArgs = emptyList<String>().toMutableList()
        for (i in headerParts.indices) {
            headerArgs.add(headerParts[i].split("\"")[2])
        }
        var toReturn = ""
        try {
            val privateKey = getPrivateKey("$basePath\\priv$port.pem")
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
        private const val ALG_SYMMETRIC = "AES"
        private const val ALG_ASYMMETRIC = "RSA"
        private const val STD_CERTIFICATE = "X.509"
        private const val MARK_SIZE = 128
        private const val KEY_SIZE = 256
        private const val PASSWORD = "changeit" // do not change it
        private const val JWE_HEADER = "{\"alg\":\"RSA\",\"enc\":\"AES/GCM/NoPadding\"}"
    }
}
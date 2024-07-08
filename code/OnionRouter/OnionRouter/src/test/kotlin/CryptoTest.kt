import java.io.File
import kotlin.random.Random
import kotlin.test.*

class CryptoTest {
    private val path = System.getProperty("user.dir") + "\\src\\test\\resources"
    private val crypto = Crypto(path)

    private fun generateRandomPort() = Random.nextInt(7000, 9000)
    private fun generateIp() = listOf("", "", "", "").joinToString(".") { it + Random.nextInt(0, 256) }

    @Test
    fun `can generate private key`(){
        val port = generateRandomPort()
        crypto.generatePrivateKey(port)

        val privKeyFile = File("$path\\priv$port.pem")

        assertTrue(privKeyFile.exists())

        val privContent = privKeyFile.readText(Charsets.UTF_8)
        assertFalse { privContent.isEmpty() }
        assertFalse { privContent.isBlank() }
    }

    @Test
    fun `can generate CSR`(){
        val port = generateRandomPort()
        val ip = "localhost:" + generateIp()
        val pwd = "P4\$\$w0rd"
        crypto.generatePrivateKey(port)
        val csrContent = crypto.generateClientCSR(port, ip, pwd).joinToString("\n")

        val csrFile = File("$path\\$port.csr")

        assertFalse(csrFile.exists())

        assertFalse { csrContent.isEmpty() }
        assertFalse { csrContent.isBlank() }
    }

    @Test
    fun `can encipher and decipher a message`() {
        val port = generateRandomPort()
        crypto.generateKeys(port)

        val message = "Hello, World!"

        val encMsg = crypto.encipher(message, port)
        assertEquals(5, encMsg.split(".").size)

        val decMsg = crypto.decipher(encMsg, port)
        assertEquals(message, decMsg)
    }

    @Test
    fun `key can't decipher`(){
        val port1 = generateRandomPort()
        val port2 = generateRandomPort()

        crypto.generateKeys(port1)
        crypto.generateKeys(port2)

        val message = "Hello, World!"

        val encMsg = crypto.encipher(message, port1)
        assertEquals(5, encMsg.split(".").size)

        val decMsg = crypto.decipher(encMsg, port2)
        assertNotEquals(message, decMsg)
    }

    @Test
    fun `can encrypt and decrypt with password`(){
        val message = "Hello, World!"
        val pwdHash = "P4\$\$w0rd".hashCode().toString()

        val encMsg = crypto.encryptWithPwd(message, pwdHash)
        val decMsg = crypto.decryptWithPwd(encMsg, pwdHash)

        assertEquals(message, decMsg)
    }

    @Test
    fun `can't decrypt with wrong password`() {
        val message = "Hello, World!"
        val pwdHash1 = "P4\$\$w0rd".hashCode().toString()
        val pwdHash2 = "P4\$\$word".hashCode().toString()

        val encMsg = crypto.encryptWithPwd(message, pwdHash1)
        val decMsg = crypto.decryptWithPwd(encMsg, pwdHash2)

        assertNotEquals(message, decMsg)
    }
}
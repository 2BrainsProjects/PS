import java.io.File
import kotlin.random.Random
import kotlin.test.*

class CryptoTest {
    private val crypto = Crypto()
    private val path = System.getProperty("user.dir") + "\\src\\test\\resources"

    private fun generateRandomPort() = Random.nextInt(7000, 9000)
    private fun generateIp() = listOf("", "", "", "").joinToString(".") { it + Random.nextInt(0, 256) }

    @Test
    fun `can generate keys`(){
        val port = generateRandomPort()
        crypto.generateKeys(port, path)

        val privKeyFile = File("$path\\priv$port.pem")
        val pubKeyFile = File("$path\\pub$port.pem")

        assertTrue(privKeyFile.exists())
        assertTrue(pubKeyFile.exists())

        val privContent = privKeyFile.readText(Charsets.UTF_8)

        assertFalse { privContent.isEmpty() }
        assertFalse { privContent.isBlank() }

        val pubContent = pubKeyFile.readText(Charsets.UTF_8)
        assertFalse { pubContent.isEmpty() }
        assertFalse { pubContent.isBlank() }
    }

    @Test
    fun `can generate CSR`(){
        val port = generateRandomPort()
        val ip = "localhost:" + generateIp()
        val pwd = "P4\$\$w0rd"
        crypto.generateClientCSR(port, ip, pwd, path)

        val csrFile = File("$path\\$port.csr")

        assertTrue(csrFile.exists())

        val csrContent = csrFile.readText(Charsets.UTF_8)

        assertFalse { csrContent.isEmpty() }
        assertFalse { csrContent.isBlank() }
    }

    @Test
    fun `can encipher and decipher a message`() {
        val port = generateRandomPort()
        crypto.generateKeys(port, path)

        val message = "Hello, World!"

        val encMsg = crypto.encipher(message, port, path)
        assertEquals(5, encMsg.split(".").size)

        val decMsg = crypto.decipher(encMsg, port, path)
        assertEquals(message, decMsg)
    }

    @Test
    fun `key can't decipher`(){
        val port1 = generateRandomPort()
        val port2 = generateRandomPort()

        crypto.generateKeys(port1, path)
        crypto.generateKeys(port2, path)

        val message = "Hello, World!"

        val encMsg = crypto.encipher(message, port1, path)
        assertEquals(5, encMsg.split(".").size)

        val decMsg = crypto.decipher(encMsg, port2, path)
        assertNotEquals(message, decMsg)
    }
}
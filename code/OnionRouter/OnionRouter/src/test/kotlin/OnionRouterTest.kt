import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import kotlin.random.Random
import kotlin.test.assertTrue

class OnionRouterTest {
    private fun generateRandomPort() = Random.nextInt(7000, 9000)

    @Test
    fun `can connect to the server`(){
        val serverPort = generateRandomPort()
        val onionRouter = OnionRouter(serverPort)
        val crypto = Crypto()

        Thread{
            onionRouter.start()
        }.start()

        val serverIp = InetSocketAddress(serverPort)
        val pwd = "P4\$\$w0rd"
        val msg = "hello"
        val nodes = listOf("127.0.0.1:$serverPort")
        val bytesWritten = clientSender(serverIp, pwd, msg, nodes)
        assertTrue(bytesWritten > 0)

        // ver, corrigir e minimizar passagens de path de um lado para o outro
        crypto.encipher(msg, serverPort)
    }
}
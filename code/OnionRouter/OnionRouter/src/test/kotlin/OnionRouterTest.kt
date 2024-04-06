import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OnionRouterTest {
    private val path = System.getProperty("user.dir") + "\\src\\test\\resources"
    private val DELAY_TO_SET_NETWORK = 1000L
    private fun generateRandomPort() = Random.nextInt(7000, 9000)

    @Test
    fun `can connect to the server`(){
        val serverPort = generateRandomPort()
        val onionRouter = OnionRouter(serverPort, path)
        println("serverPort: $serverPort")
        Thread{
            onionRouter.start()
        }.start()

        val serverIp = InetSocketAddress(serverPort)
        val pwd = "P4\$\$w0rd"
        val msg = "hello"
        val nodes = emptyList<String>()

        Thread.sleep(DELAY_TO_SET_NETWORK)

        val bytesWritten = clientSender(serverIp, pwd, msg, nodes, path)
        assertTrue(bytesWritten > 0)
    }
}
import org.junit.jupiter.api.Test
import java.net.BindException
import java.net.InetSocketAddress
import java.net.ServerSocket
import kotlin.random.Random
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OnionRouterTest {
    private val path = System.getProperty("user.dir") + "\\src\\test\\resources"
//  private val DELAY_TO_SET_NETWORK = 1000L
    private fun generateRandomPort() = Random.nextInt(7000, 9000)

    /*@Test
    fun `can create onion router`(){
        val serverPort = generateRandomPort()
        val onionRouter = OnionRouter(InetSocketAddress(serverPort), path)
        Thread{
            onionRouter.start()
        }.start()

        assertFailsWith<BindException> { ServerSocket().bind(InetSocketAddress(serverPort)) }
    }
    */

    /*
    @Test
    fun `can connect to the server`(){
        val serverPort = generateRandomPort()
        val onionRouter = OnionRouter(InetSocketAddress(serverPort), path)
        println("serverPort: $serverPort")
        Thread{
            onionRouter.start()
        }.start()

        val serverIp = InetSocketAddress(serverPort)
        val pwd = "P4\$\$w0rd"
        val msg = "hello"
        val nodes = emptyList<String>()

        Thread.sleep(DELAY_TO_SET_NETWORK)

        val bytesWritten = clientSender(serverIp, msg, nodes, path)
        assertTrue(bytesWritten > 0)
    }
    */

    /*
    @Test
    fun `one client can communicate with another`(){
        val serverPort = generateRandomPort()
        println("serverPort: $serverPort")
        val onionRouter = OnionRouter(serverPort, path)

        Thread{
            onionRouter.start()
        }.start()

        val lastClientIp = InetSocketAddress(generateRandomPort())
        val pwdClient = "P4\$\$w0rd"
        val finalMsg: AtomicReference<String> = AtomicReference("")

        Thread.sleep(DELAY_TO_SET_NETWORK)

        val t = Thread{
            println("last client ip ${lastClientIp.port}")
            finalMsg.set(clientReceiver(lastClientIp, pwdClient, path))
        }
        t.start()

        val serverIp = InetSocketAddress(serverPort)
        val pwd = "P4\$\$w0rd"
        val msg = "hello"
        val nodes = listOf("127.0.0.1:${lastClientIp.port}")

        Thread{
            println("sender to server ${serverIp.port}")
            clientSender(serverIp, pwd, msg, nodes, path)
        }.start()

        assertEquals(msg, finalMsg.get())
    }
    */
}
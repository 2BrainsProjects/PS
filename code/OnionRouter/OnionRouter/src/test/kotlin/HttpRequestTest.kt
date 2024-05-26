import domain.Message
import domain.UserStorage
import http.HttpRequests
import kotlin.random.Random
import kotlin.test.*

class HttpRequestTest {

    private val crypto = Crypto()
    private val httpRequests = HttpRequests(crypto)

    private fun userInformation() = Triple(generateName(), generateIp(), "P4\$\$w0rd")

    private fun routerInformation() = Pair(generateIp(), "P4\$\$w0rd")
    private fun generateName() = List(10) { ('a'..'z').random() }.joinToString("")
    private fun generateIp() = List(4) { Random.nextInt(0, 256) }.joinToString(".") + ":" + generateRandomPort()
    private fun generateRandomPort() = Random.nextInt(7000, 9000)
    private fun generateCsr(ip: String, pwd: String) = crypto.generateClientCSR(ip.split(":").last().toInt(), ip, pwd).joinToString("\n")


    @Test
    fun `can register a client`(){
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
        assertTrue(id > 0)
    }

    @Test
    fun `cant register a client with wrong credentials`() {
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)

        assertFailsWith<Exception>{ httpRequests.registerClient(name, name, pwd, csr) }
        assertFailsWith<Exception>{ httpRequests.registerClient(name, name, "password", csr) }
        httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
        assertFailsWith<Exception>{ httpRequests.registerClient(name, "$name@gmail.com", pwd, csr) }
    }

    @Test
    fun `can login a client`() {
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)

        val (token, userStorage) = httpRequests.loginClient(name, ip, pwd)

        assertNull(userStorage)

        val client = httpRequests.getClient(token.token)
        assertEquals(client.id, id)
        assertEquals(client.name, name)
    }

    @Test
    fun `cant login a client with wrong credentials`() {
        val (name, ip, pwd) = userInformation()
        assertFailsWith<Exception>{ httpRequests.loginClient(name, ip, pwd) }
    }

    @Test
    fun `can logout a client`() {
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
        val (token, _) = httpRequests.loginClient(name, ip, pwd)
        val userStorage = UserStorage(id, name, emptyList())
        val logout = httpRequests.logoutClient(pwd, token.token, userStorage)

        assertTrue(logout)
        assertFailsWith<Exception> { httpRequests.getClient(token.token) }
    }

    @Test
    fun `can save and get Messages`() {
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)
        httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)

        val (token, _) = httpRequests.loginClient(name, ip, pwd)

        val cid = "0"

        val messagesToSave = listOf(
            Message(cid, "Hello, World!", "2021-10-10 10:10:10"),
            Message(cid, "How are you doing?", "2021-10-10 10:10:11")
        )

        messagesToSave.forEach {
            assertTrue { httpRequests.saveMessage(token.token, it.conversationId, it.content, it.timestamp) }
        }

        val messages = httpRequests.getMessages(token.token, cid)
        assertEquals(messagesToSave.size, messages.size)
        assertTrue(messages.containsAll(messagesToSave))

        val messages2 = httpRequests.getMessages(token.token, cid, "2021-10-10 10:10:10")

        assertEquals(1, messages2.size)
        assertEquals(messagesToSave[1], messages2[0])
    }

    @Test
    fun `can get Clients`(){
        val (name, ip, pwd) = userInformation()
        val csr = generateCsr(ip, pwd)
        httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
        val ids = List(2){
            val (name, ip, pwd) = userInformation()
            val csr = generateCsr(ip, pwd)
            httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
        }

        val clientCount = httpRequests.getClientCount()
        assertTrue(ids.last() <= clientCount)

        val clients = httpRequests.getClients(ids + (clientCount+1))
        assertEquals(ids.size, clients.size)
        assertEquals(ids.size, clients.filter { ids.contains(it.id) }.size)
    }


    @Test
    fun `can register a onion router`() {
        val (ip, pwd) = routerInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerOnionRouter(csr, ip, pwd)
        assertTrue(id > 0)
    }

    @Test
    fun `can delete a onion router`() {
        val (ip, pwd) = routerInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerOnionRouter(csr, ip, pwd)
        val deleted = httpRequests.deleteRouter(id, pwd)
        assertTrue(deleted)

    }

    @Test
    fun `cant delete a onion router with wrong credentials`() {
        val (ip, pwd) = routerInformation()
        val csr = generateCsr(ip, pwd)
        val id = httpRequests.registerOnionRouter(csr, ip, pwd)
        assertFailsWith<Exception>{ httpRequests.deleteRouter(id, "password") }
    }

    @Test
    fun `can get onion routers`(){
        val ids = List(2){
            val (ip, pwd) = routerInformation()
            val csr = generateCsr(ip, pwd)
            httpRequests.registerOnionRouter(csr, ip, pwd)
        }

        val routerCount = httpRequests.getRouterCount()
        assertTrue(ids.last() <= routerCount)

        val routers = httpRequests.getRouters(ids + (routerCount+1))
        assertEquals(ids.size, routers.size)
        assertEquals(ids.size, routers.filter { ids.contains(it.id) }.size)
    }
}
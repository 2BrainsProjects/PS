import domain.Contact
import domain.Message
import http.HttpRequests
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import kotlin.test.*

class LocalMemoryTest {
    private val crypto = Crypto()
    private val httpRequests = HttpRequests(crypto)
    private val localMemory = LocalMemory(httpRequests, crypto)
    @Test
    fun `can store and retrieve session file from memory`(){
        val (name, ip, pwd) = userInformation()
        val pwdHash = pwd.hashCode().toString()
        val id = registerClient(name, ip, pwd)
        val (_, userStorage) = loginClient(name, ip, pwd)
        assertNull(userStorage)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)
        localMemory.saveSession(id, current, pwdHash)
        val session = localMemory.getSession(id, pwdHash)
        assertNotNull(session)
        assertEquals(current, session)
    }

    @Test
    fun `can store and retrieve messages from local memory`(){
        val (_, _, pwd) = userInformation()
        val pwdHash = pwd.hashCode().toString()

        val contactName = generateName()
        val contact = Contact(0, contactName)

        assertTrue(localMemory.getMessages(contact.name, pwdHash).isEmpty())

        val messagesToSave = listOf(
            Message("${contact.id}", "Hello ${contact.name}", "2021-10-10 10:10:10"),
            Message("${contact.id}", "How are you doing?", "2021-10-10 10:10:11")
        )

        for (msg in messagesToSave){
            localMemory.saveMessageInFile(msg, pwdHash, contact.name)
        }

        val messages = localMemory.getMessages(contact.name, pwdHash)
        assertEquals(messagesToSave.size, messages.size)
        assertTrue(messages.containsAll(messagesToSave))
    }

    @Test
    fun `can store and retrieve messages from Api`() {
        val (name, ip, pwd) = userInformation()
        val pwdHash = pwd.hashCode().toString()
        val id = registerClient(name, ip, pwd)
        val (token, userStorage) = loginClient(name, ip, pwd)
        assertNull(userStorage)

        val (contactName, contactIp, contactPwd) = userInformation()
        val contact = Contact( registerClient(contactName, contactIp, contactPwd), contactName)

        val cid = localMemory.buildCid(id, contact.id, pwdHash)
        val messagesToSave = listOf(
            Message(cid, "Hello ${contact.name}", "2021-10-10 10:10:10"),
            Message(cid, "How are you doing?", "2021-10-10 10:10:11")
        )

        for (msg in messagesToSave){
            localMemory.saveMessageInFile(msg, pwdHash, contact.name)
        }

        localMemory.saveMessages(token.token, "2021-10-10 10:10:09", listOf((contact)))
        localMemory.deleteConversation(contact.name)
        localMemory.contactsFilesSetup(id, pwdHash, token.token, listOf(contact))

        val messages = localMemory.getMessages(contact.name, pwdHash)
        assertEquals(messagesToSave.size, messages.size)
        assertTrue(messages.containsAll(messagesToSave))
    }

    private fun registerClient(name: String, ip:String, pwd: String): Int {
        val port = ip.split(":")[1].toInt()
        crypto.generatePrivateKey(port)
        val csr =  crypto.generateClientCSR(port, ip, pwd).joinToString ("\n")
        return httpRequests.registerClient(name, "$name@gmail.com", pwd, csr)
    }

    private fun loginClient(name: String, ip: String, pwd: String) =
        httpRequests.loginClient(name, ip, pwd)
    
    private fun userInformation() = Triple(generateName(), generateIp(), "P4\$\$w0rd")

    private fun generateName() = List(10) { ('a'..'z').random() }.joinToString("")
    private fun generateIp() = List(4) { Random.nextInt(0, 256) }.joinToString(".") + ":" + generateRandomPort()
    private fun generateRandomPort() = Random.nextInt(7000, 9000)
}
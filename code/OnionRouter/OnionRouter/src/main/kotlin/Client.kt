import commands.Login
import commands.Logout
import commands.Register
import domain.*
import http.HttpRequests
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

class Client(
    private val crypto: Crypto,
    private val httpRequests: HttpRequests,
    private val sendMsg: (ClientInformation, String, String) -> String
) {
    private var routerStorage: RouterStorage? = null
    private var userStorage: Session? = null
    private val localMemory = LocalMemory(httpRequests, crypto)
    private val pathSize = 2
    private val amountRequest = 4

    /*
    initialization menu
        - client
        - router
        - both

    authentication menu (if choice client ou both)
        - register
        - login

    operations menu
        - add contact
        - contact messages
        - list contacts
        - send message
        - logout
    */

    fun getInfo(): Pair<Session?, RouterStorage?> = Pair(userStorage, routerStorage)

    fun deleteNode(){
        val userStorage = userStorage
        val routerStorage = routerStorage
        if (userStorage != null) {
            val pwd = userStorage.pwd
            val token = userStorage.token
            if (pwd != null && token != null)
                Logout(httpRequests, userStorage, localMemory).execute(listOf(pwd, token.token))
        }
        if (routerStorage != null) {
            httpRequests.deleteRouter(routerStorage.id, routerStorage.pwd)
        }
    }

    fun initializationMenu(ip: String){
        showMenu(
            "Would you like to initialize as:",
            "1 - Client",
            "2 - Router",
            "3 - Both"
        )
        val command = readln()
        when (command) {
            "1" -> {
                authenticationMenu(ip)
                operationsMenu()
            }
            "2" -> {
                initializeRouter(ip)
            }
            "3" -> {
                val csr = authenticationMenu(ip)
                initializeRouter(ip, csr)
                operationsMenu()
            }
        }
    }

    private fun authenticationMenu(ip: String):String? {
        var command: String
        var csr : String? = null
        while (true) {
            showMenu(
                "Menu",
                "1 - Register",
                "2 - Login"
            )
            command = readln()
            when (command) {
                "1" -> {
                    println("Register")

                    val args = getInputs(listOf("Name", "Email", "Password")).toMutableList()
                    val port = ip.split(":").last().toInt()
                    val name = args[0]
                    val pwd = args[2]
                    csr = crypto.generateClientCSR(port, name, pwd).joinToString("\n")
                    args.add(csr)
                    args.add(ip)
                    try{
                        userStorage = Session()
                        Register(httpRequests, userStorage!!, localMemory).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
                "2" -> {
                    println("Login")
                    val args = getInputs(listOf("Name or Email", "Password")).toMutableList()

                    args.add(1, ip)
                    try {
                        userStorage = Session()
                        Login(httpRequests, userStorage!!, localMemory).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
            }
        }
        return csr
    }

    private fun operationsMenu(){
        var command: String
        while (true) {
            showMenu(
                "Menu",
                "1 - Add contact",
                "2 - Contact messages",
                "3 - List contacts",
                "4 - Logout"
            )
            command = readln()
            when (command) {
                "1" -> {
                    var clientId: Int?
                    while(true) {
                        val args = getInputs(listOf("Enter id of the contact"))
                        clientId = args.first().toIntOrNull()
                        if (clientId != null) {
                            val client = getClientData(clientId)
                            if (client == null) {
                                println("Client not found")
                                break
                            }
                            userStorage?.contacts?.add(Contact(clientId, client.name))
                            println("User ${client.name} added!")
                            break
                        }
                    }
                }
                "2" -> {
                    val args = getInputs(listOf("Name of the contact"))

                    val contactId = userStorage?.contacts?.firstOrNull { it.name == args.first() }?.id

                    if(contactId == null) {
                        println("Contact not found")
                        continue
                    }

                    val client = getClientData(contactId)
                    if(client == null) {
                        println("Contact not found")
                        continue
                    }

                    val msgs = localMemory.getMessages(args[0], userStorage?.pwd!!)

                    val cid = localMemory.buildCid(userStorage?.id!!, contactId, userStorage?.pwd!!)

                    /*for(i in msgs.takeLast(10)) {
                        if(i.content.split(":").first().toInt() == userStorage?.id) {
                            println(rgbfg(0,255,0) + i + RC)
                        } else {
                            println(i)
                        }
                    }*/

                    while (true){
                        println("1 - Send message")
                        println("2 - Load previous messages")
                        println("3 - Load next messages")
                        println("4 - Exit")
                        print("> ")
                        val option = readln()

                        when (option) {
                            "1" -> {
                                val msg = getInputs(listOf("Message")).first()

                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                val msgDate = LocalDateTime.now().format(formatter)

                                val sentMsg = sendMsg(client, msg, msgDate)

                                val message = Message(cid, sentMsg, msgDate)
                                localMemory.saveMessageInFile(message, userStorage?.pwd!!, client.name)
                            }
                            "2" -> {
                                // sliding window to get the files(?)
                            }
                            "3" -> {

                            }
                            "4" -> {
                                break
                            }
                        }
                    }
                }
                "3" -> {
                    // sliding window to get the files(?)
                }
                "4" -> {
                    deleteNode()
                    println("Logout successfully.")
                    break
                }
            }
        }
    }

    private fun getClientData(clientId: Int): ClientInformation? {
        val count = httpRequests.getClientCount()
        val ids: MutableList<Int> = (0..count).shuffled().take(amountRequest).toMutableList()

        ids.add(clientId)
        val idsListToSend = ids.toSet().shuffled()

        val list = httpRequests.getClients(idsListToSend)

        val client = list.firstOrNull { it.id == clientId }

        return client
    }

    fun buildMessagePath(): List<Router> {
        val count = httpRequests.getRouterCount()
        val ids = (0..count).shuffled().take(amountRequest)

        val list = httpRequests.getRouters(ids)

        val pathRouters = list.shuffled().take(pathSize)
        return pathRouters
    }

    fun encipherMessage(
        message: String,
        list: List<Pair<String, X509Certificate>>,
    ): String {
        var finalMsg = message

        for (i in 0 until list.size - 1) {
            val element = list[i]
            finalMsg = crypto.encipher(finalMsg, element.second)
            finalMsg += "||${element.first}"
        }
        finalMsg = crypto.encipher(finalMsg, list.last().second)
        return finalMsg
    }


    private fun initializeRouter(ip: String, csr: String? = null){
        val password = "Pa\$\$w0rd${Random.nextInt()}"
        val port = ip.split(":").last().toInt()
        println("running on port $port")
        val csrToUser = csr ?: crypto.generateClientCSR(port, "router", password).joinToString("\n")
        val routerId = httpRequests.registerOnionRouter(csrToUser, ip , password)
        routerStorage = RouterStorage(routerId, password)
    }

    private fun showMenu(vararg options: String) {
        println("___________________________________")
        options.forEach { println(it) }
        print("> ")
    }

    private fun getInputs(list: List<String>): List<String>{
        val inputs = mutableListOf<String>()
        for (i in list){
            var input = ""
            while (input.isEmpty() || input.isBlank()) {
                println("$i ")
                print("> ")
                input = readln()
            }
            inputs.add(input)
        }
        return inputs
    }
}

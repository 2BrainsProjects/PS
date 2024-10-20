import commands.Login
import commands.Logout
import commands.Register
import domain.*
import http.HttpRequests
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import kotlin.random.Random

const val ONE_MINUTE = 60000

class Client(
    private val crypto: Crypto,
    private val httpRequests: HttpRequests,
    private val sendMsg: (ClientInformation, String) -> String,
) {
    private var routerStorage: RouterStorage? = null
    private var session: Session? = null
    private val localMemory = LocalMemory(httpRequests, crypto)
    private val pathSize = 2
    private val amountRequest = 4
    private val messages = emptyList<String>().toMutableList()
    private val messagesPerPage = 10

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

    fun getInfo(): Pair<Session?, RouterStorage?> = Pair(session, routerStorage)

    fun deleteNode(port: Int) {
        val userStorage = session
        val routerStorage = routerStorage
        if (userStorage != null) {
            val pwd = userStorage.pwd
            val token = userStorage.token
            val privateKey = crypto.getPrivateKey(port)
            if (pwd != null && token != null) {
                Logout(httpRequests, userStorage, localMemory).execute(listOf(pwd, token.token, privateKey))
            }
        }
        if (routerStorage != null) {
            httpRequests.deleteRouter(routerStorage.id, routerStorage.pwd)
        }
    }

    fun readFinalMsg(msg: String) {
        // final:id:name:msg:timestamp
        val (idContact, name, message, timestamp) = extractDataFromMessage(msg)
        val id = session?.id
        val pwd = session?.pwd
        if (id != null && idContact != null && pwd != null) {
            val cid = localMemory.buildCid(id, idContact, pwd)
            val msgToSave = Message(cid, "$idContact:$name:$message", timestamp)
            localMemory.saveMessageInFile(msgToSave, pwd, name)
            val client = getClientData(idContact)
            println("$name:$message <$timestamp>")
            if (client != null) {
                val current = LocalDateTime.now().format()

                sendMsg(client, "confirmation:$idContact:${session?.name}:$message:$current")
            }
        }
    }

    fun readConfirmationMsg(msg: String) {
        // confirmation:id:name:msg
        val (_, name, message, timestamp) = extractDataFromMessage(msg)
        // println("removed msg: ${session?.id}:${session?.name}:$message")
        messages.remove("${session?.id}:${session?.name}:$message")
        println("$name received the message: $message at $timestamp")
    }

    fun initializationMenu(ip: String) {
        showMenu(
            "Would you like to initialize as:",
            "1 - Client",
            "2 - Router",
            "3 - Both",
        )
        try {
            val command = readln()
            when (command) {
                "1" -> {
                    authenticationMenu(ip)
                    operationsMenu(ip)
                }
                "2" -> {
                    initializeRouter(ip)
                }
                "3" -> {
                    authenticationMenu(ip)
                    initializeRouter(ip)
                    operationsMenu(ip)
                }
            }
        } catch (e: Exception) {
            println("Something went wrong. Try again.")
            println(e.message)
        }
    }

    fun buildMessagePath(ipClient: String): List<Router> {
        val count = httpRequests.getRouterCount()
        var ids: Set<Int>
        var list: List<Router> = emptyList()

        // if(ids.size <= 1) throw Exception("Not enough routers to build a path")
        var counter = 0

        do {
            ids = (0..count).shuffled().take(amountRequest).toMutableSet()
            counter++
            if (counter >= 5 || count <= amountRequest) {
                ids.remove(routerStorage?.id)
                list = httpRequests.getRouters(ids.toList())
                val temp = list.toMutableList()
                temp.removeIf { it.ip == ipClient }
                list = temp
                break
            }
            if (ids.contains(routerStorage?.id)) continue

            list = httpRequests.getRouters(ids.toList()).toMutableList()
        } while (list.any { it.ip != ipClient })

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

    private fun authenticationMenu(ip: String) {
        var command: String
        while (true) {
            showMenu(
                "Menu",
                "1 - Register",
                "2 - Login",
            )
            command = readln()
            when (command) {
                "1" -> {
                    println("Register")

                    val args = getInputs(listOf("Name", "Email", "Password")).toMutableList()
                    val port = ip.split(":").last().toInt()
                    val name = args[0]
                    val pwd = args[2]
                    crypto.generatePrivateKey(port)
                    val csr = crypto.generateClientCSR(port, name, pwd).joinToString("\n")
                    args.add(csr)
                    args.add(ip)
                    try {
                        session = Session()
                        Register(httpRequests, session!!, localMemory).execute(args)
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
                        session = Session()
                        Login(httpRequests, session!!, localMemory).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
            }
        }
    }

    private fun operationsMenu(ip: String) {
        val port = ip.split(":").last().toInt()
        var command: String
        while (true) {
            showMenu(
                "Menu",
                "1 - Show my personal details",
                "2 - Add contact",
                "3 - Contact messages",
                "4 - List contacts",
                "5 - Logout",
            )
            command = readln()
            clearConsole()
            when (command) {
                "1" -> {
                    println("Name: ${session?.name}")
                    println("Id: ${session?.id}")
                }
                "2" -> {
                    addContact()
                }
                "3" -> {
                    val args = getInputs(listOf("Name of the contact"))

                    val contactId = session?.contacts?.firstOrNull { it.name == args.first() }?.id

                    if (contactId == null) {
                        println("Contact not found")
                        continue
                    }

                    val client = getClientData(contactId)
                    if (client == null) {
                        println("Contact not found")
                        continue
                    }

                    conversationMenu(client)
                }
                "4" -> {
                    println("Contacts: \n ${session?.contacts?.joinToString("\n") { "${it.id} - ${it.name}" }}")
                    // sliding window to get the files(?)
                }
                "5" -> {
                    deleteNode(port)
                    println("Logout successfully.")
                    break
                }
            }
        }
    }

    private fun conversationMenu(client: ClientInformation) {
        val id = session?.id
        val pwd = session?.pwd
        val name = session?.name

        if (id == null || pwd == null || name == null) {
            println("Something went wrong. Try again.")
            return
        }

        val cid = localMemory.buildCid(id, client.id, pwd)
        var n = 0

        val m = localMemory.getMessagesPage(client.name, n, messagesPerPage, pwd)
        m.forEach { println(it.content + " <" + it.timestamp + ">") }

        while (true) {
            showMenu(
                "Conversation of ${client.name}",
                "1 - Send message",
                "2 - Load previous messages",
                "3 - Load next messages",
                "4 - Exit",
            )
            val option = readln()
            clearConsole()

            when (option) {
                "1" -> {
                    val msg = getInputs(listOf("Message")).first()

                    val msgDate = LocalDateTime.now().format()
                    val msgToSend = "final:$id:$name:$msg:$msgDate"

                    // id:name:msg
                    val sentMsg = sendMsg(client, msgToSend).replace("final:", "").replace(":$msgDate", "")
                    Thread {
                        // println("added msg: $sentMsg")
                        messages.add(sentMsg)
                        val timer = System.currentTimeMillis()
                        while (System.currentTimeMillis() - timer < ONE_MINUTE);

                        val splitMsg = sentMsg.split(":")
                        val message = splitMsg.drop(2).joinToString(":")
                        if (messages.contains(sentMsg)) {
                            println("Your message: $message to ${client.name} wasn't delivered!")
                            messages.remove(msgToSend)
                        }
                    }.start()

                    val message = Message(cid, sentMsg, msgDate)
                    localMemory.saveMessageInFile(message, pwd, client.name)
                }
                "2" -> {
                    if (localMemory.hasMessagesInPage(client.name, n + 1, messagesPerPage, pwd)) {
                        val messages = localMemory.getMessagesPage(client.name, ++n, messagesPerPage, pwd)
                        messages.forEach { println(it.content + " <" + it.timestamp + ">") }
                    }
                }
                "3" -> {
                    val newPage = if (n - 1 >= 0) n - 1 else n
                    if (newPage >= 0) {
                        if (localMemory.hasMessagesInPage(client.name, newPage, messagesPerPage, pwd)) {
                            n = newPage
                            val messages = localMemory.getMessagesPage(client.name, n, messagesPerPage, pwd)
                            messages.forEach { println(it.content + " <" + it.timestamp + ">") }
                        }
                    }
                }
                "4" -> {
                    break
                }
            }
        }
    }

    private fun addContact() {
        while (true) {
            println("-1 - Exit")
            val args = getInputs(listOf("Enter id of the contact"))
            if (args.first() == "-1") break
            val clientId = args.first().toIntOrNull()
            if (clientId != null) {
                if (clientId == session?.id || clientId < 0) continue
                if (session?.contacts?.firstOrNull { it.id == clientId } != null) {
                    println("Contact already added!")
                    continue
                }
                val client = getClientData(clientId)
                if (client == null) {
                    println("Client not found")
                    continue
                }
                session?.contacts?.add(Contact(clientId, client.name))
                println("User ${client.name} added!")
                break
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

    private fun initializeRouter(ip: String) {
        val password = "Pa\$\$w0rd${Random.nextInt()}"
        val port = ip.split(":").last().toInt()
        println("running on port $port")
        val privateKey = crypto.getPrivateKey(port)
        if (privateKey.isEmpty() || privateKey.isBlank()) crypto.generatePrivateKey(port)
        val csrToUse = crypto.generateClientCSR(port, "router", password).joinToString("\n")
        val routerId = httpRequests.registerOnionRouter(csrToUse, ip, password)
        routerStorage = RouterStorage(routerId, password)
    }

    private fun showMenu(vararg options: String) {
        println("___________________________________")
        options.forEach { println(it) }
        print("> ")
    }

    private fun getInputs(list: List<String>): List<String> {
        val inputs = mutableListOf<String>()
        for (i in list) {
            var input = ""
            while (input.isEmpty() || input.isBlank()) {
                println("$i ")
                print("> ")
                input = readln()
            }
            inputs.add(input)
        }
        clearConsole()
        return inputs
    }

    private fun extractDataFromMessage(msg: String): MessageData {
        val info = msg.split(":").drop(1)
        val idContact = info.first().toIntOrNull()
        val name = info[1]
        val message = info.dropLast(3).drop(2).joinToString(":")
        val timestamp = info.takeLast(3).joinToString(":")
        return MessageData(idContact, name, message, timestamp)
    }

    private fun clearConsole() {
        print("\u001b[H\u001b[2J")
        /*repeat(40) {
            println("\n")
        }*/
    }
}

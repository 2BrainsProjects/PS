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
    private val sendMsg: (ClientInformation, String, String) -> String,
) {
    private var routerStorage: RouterStorage? = null
    private var session: Session? = null
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
            val msgToSave = Message(cid, message, timestamp)
            localMemory.saveMessageInFile(msgToSave, pwd, name)
            val client = getClientData(idContact)
            if (client != null) {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val current = LocalDateTime.now().format(formatter)
                sendMsg(client, "confirmation:$message at $current", current)
                // sendMsg(client, msgToSend, msgDate).replace("final:", "")
            }
        }
    }

    fun readConfirmationMsg(msg: String) {
        // confirmation:id:name:msg
        val (_, name, message, timestamp) = extractDataFromMessage(msg)
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
                    val csr = authenticationMenu(ip)
                    initializeRouter(ip, csr)
                    operationsMenu(ip)
                }
            }
        } catch (e: Exception) {
            println("Something went wrong. Try again.")
            println(e.message)
        }
    }

    fun buildMessagePath(): List<Router> {
        val count = httpRequests.getRouterCount()
        val ids = (0..count).shuffled().take(amountRequest)

        // if(ids.size <= 1) throw Exception("Not enough routers to build a path")

        while (ids.first() == routerStorage?.id) {
            ids.shuffled()
        }

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

    private fun authenticationMenu(ip: String): String? {
        var command: String
        var csr: String? = null
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
                    csr = crypto.generateClientCSR(port, name, pwd).joinToString("\n")
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
        return csr
    }

    private fun operationsMenu(ip: String) {
        val port = ip.split(":").last().toInt()
        var command: String
        while (true) {
            showMenu(
                "Menu",
                "1 - Add contact",
                "2 - Contact messages",
                "3 - List contacts",
                "4 - Logout",
            )
            command = readln()
            when (command) {
                "1" -> {
                    var clientId: Int?
                    while (true) {
                        val args = getInputs(listOf("Enter id of the contact"))
                        clientId = args.first().toIntOrNull()
                        if (clientId != null) {
                            val client = getClientData(clientId)
                            if (client == null) {
                                println("Client not found")
                                break
                            }
                            session?.contacts?.add(Contact(clientId, client.name))
                            println("User ${client.name} added!")
                            break
                        }
                    }
                }
                "2" -> {
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

                    val msgs = localMemory.getMessages(args[0], session?.pwd!!)

                    val cid = localMemory.buildCid(session?.id!!, contactId, session?.pwd!!)

                    /*for(i in msgs.takeLast(10)) {
                        if(i.content.split(":").first().toInt() == userStorage?.id) {
                            println(rgbfg(0,255,0) + i + RC)
                        } else {
                            println(i)
                        }
                    }*/

                    while (true) {
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
                                val msgToSend = "final:${session?.id}:${session?.name}:$msg:$msgDate"
                                val sentMsg = sendMsg(client, msgToSend, msgDate).replace("final:", "")

                                val message = Message(cid, sentMsg, msgDate)
                                localMemory.saveMessageInFile(message, session?.pwd!!, client.name)
                            }
                            "2" -> {
                                println("Not implemented yet")
                                // sliding window to get the files(?)
                            }
                            "3" -> {
                                println("Not implemented yet")
                            }
                            "4" -> {
                                break
                            }
                        }
                    }
                }
                "3" -> {
                    println("Not implemented yet")
                    // sliding window to get the files(?)
                }
                "4" -> {
                    deleteNode(port)
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

    private fun initializeRouter(
        ip: String,
        csr: String? = null,
    ) {
        val password = "Pa\$\$w0rd${Random.nextInt()}"
        val port = ip.split(":").last().toInt()
        println("running on port $port")
        val csrToUse = csr ?: crypto.generateClientCSR(port, "router", password).joinToString("\n")
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
        return inputs
    }

    private fun extractDataFromMessage(msg: String): MessageData {
        val info = msg.split(":").drop(1)
        val idContact = info.first().toIntOrNull()
        val name = info[1]
        val message = info.dropLast(3).joinToString(":")
        val timestamp = info.takeLast(3).joinToString(":")
        return MessageData(idContact, name, message, timestamp)
    }
}

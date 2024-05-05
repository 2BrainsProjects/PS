import commands.Login
import commands.Register
import domain.RouterStorage
import domain.UserStorage
import http.HttpRequests
import kotlin.random.Random

fun main() {
    val crypto = Crypto()
    val httpRequests = HttpRequests(crypto)
    val client = Client(crypto, httpRequests)
    client.initializationMenu("127.0.0.1:8081")
}

class Client(
    private val crypto: Crypto,
    private val httpRequests: HttpRequests
) {
    private var routerStorage: RouterStorage? = null
    private var userStorage: UserStorage? = null

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

    fun deleteNode(){
        val userStorage = userStorage
        val routerStorage = routerStorage
        if (userStorage != null) {
            userStorage.token?.let { httpRequests.logoutClient(it.token) }
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
                //operationsMenu()
            }
            "2" -> {
                initializeRouter(ip)
            }
            "3" -> {
                initializeRouter(ip)
                authenticationMenu(ip)
                //operationsMenu()
            }
        }
    }

    fun operationsMenu(){
        var command = ""
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
                "exit" -> {
                    break
                }
                "1" -> {
                    // poder adicionar por nome ou por id (?)
                    println("Enter the name of the contact")
                }
                "2" -> {
                    println("Name of the contact:")
                    // ---------------------------
                    println("Send message")
                }
                "3" -> {
                    // sliding window to get the files(?)
                }
                "4" -> {
                    println("Logout")
                }
            }
        }
    }

    private fun authenticationMenu(ip: String) {
        var command: String
        while (true) {
            showMenu(
                "Menu",
                "1 - Register",
                "2 - Login"
            )
            command = readln()
            when (command) {
                "exit" -> {
                    break
                }
                "1" -> {
                    println("Register")

                    val args = getInputs(listOf("Name", "Email", "Password")).toMutableList()
                    val port = ip.split(":").last().toInt()
                    val name = args[0]
                    val pwd = args[2]
                    val csr = crypto.generateClientCSR(port, name, pwd).joinToString("\n")
                    args.add(csr)
                    args.add(ip)
                    try{
                        userStorage = UserStorage()
                        Register(httpRequests, userStorage!!).execute(args)
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
                        userStorage = UserStorage()
                        Login(httpRequests, userStorage!!).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
            }
        }
    }

    private fun initializeRouter(ip: String){
        val password = "Pa\$\$w0rd${Random.nextInt()}"
        val port = ip.split(":").last().toInt()
        val csr = crypto.generateClientCSR(port, "router", password)
        val routerId = httpRequests.registerOnionRouter(csr.joinToString("\n"), ip , password)
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
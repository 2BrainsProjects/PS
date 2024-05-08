import commands.Login
import commands.Register
import domain.RouterStorage
import domain.UserStorage
import http.HttpRequests
import kotlin.random.Random

class Client(
    private val crypto: Crypto,
    private val httpRequests: HttpRequests,
    private val sendMsg: (Int, String) -> Unit
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

    fun getInfo(): Pair<UserStorage?, RouterStorage?> = Pair(userStorage, routerStorage)

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
        return csr
    }

    private fun operationsMenu(){
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
                "1" -> {
                    // poder adicionar por nome ou por id (?)
                    println("Enter the name or id of the contact")
                }
                "2" -> {
                    val args = getInputs(listOf("Id of the contact", "Message"))
                    sendMsg(args[0].toInt(), args[1])
                    //println("Load more")
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

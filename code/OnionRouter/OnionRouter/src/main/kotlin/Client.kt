import commands.Login
import commands.Register
import http.HttpRequests
import java.net.InetSocketAddress

fun main() {
    val crypto = Crypto()
    val httpRequests = HttpRequests(crypto)
    val client = Client(crypto, httpRequests)
    client.authenticationMenu(InetSocketAddress("127.0.0.1", 8081))
}

class Client(
    private val crypto: Crypto,
    private val httpRequests: HttpRequests
) {

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
    fun authenticationMenu(ip: InetSocketAddress){
        var command = ""
        while (true) {
            println("___________________________________")
            println("Menu")
            println("1 - Register")
            println("2 - Login")
            print("> ")
            command = readln()
            when (command) {
                "exit" -> {
                    break
                }
                "1" -> {
                    println("Register")

                    val args = getInputs(listOf("Name", "Email", "Password")).toMutableList()
                    val port = ip.port
                    val name = args[0]
                    val pwd = args[2]
                    val csr = crypto.generateClientCSR(port, name, pwd).joinToString("\n")
                    args.add(csr)

                    try{
                        Register(httpRequests).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
                "2" -> {
                    println("Login")
                    val args = getInputs(listOf("Name or Email", "Password")).toMutableList()

                    args.add(1, ip.toString().dropWhile { it == '/' })
                    try {
                        Login(httpRequests).execute(args)
                        break
                    } catch (e: Exception) {
                        println("Something went wrong. Try again.")
                        println(e.message)
                    }
                }
            }
        }
    }

    fun operationsMenu(){
        var command = ""
        while (true) {
            println("Menu")
            println("1 - Add contact")
            println("2 - Contact messages")
            println("3 - List contacts")
            println("4 - Logout")
            print(">")
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
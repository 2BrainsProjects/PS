package commands

class Login : Command {
    override val id = 2

    override fun execute(args: List<String>) { // name, email, ip, password
        println("Login command executed")
    }
}

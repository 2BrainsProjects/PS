package commands

class Register : Command {
    override val id = 1

    override fun execute(args: List<String>) { // name, email, password, clientCSR
        println("Register command executed")
    }
}

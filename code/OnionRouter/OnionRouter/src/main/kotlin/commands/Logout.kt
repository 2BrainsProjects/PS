package commands

class Logout : Command {
    override val id = 3

    override fun execute(args: List<String>) { // Nao tenho a certeza
        println("Logout command executed")
    }
}

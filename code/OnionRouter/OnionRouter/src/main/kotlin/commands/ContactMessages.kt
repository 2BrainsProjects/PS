package commands

class ContactMessages : Command {
    override val id = 6

    override fun execute(args: List<String>) {
        println("ContactMessages command executed")
    }
}

package commands

class ListContacts : Command {
    override val id = 4

    override fun execute(args: List<String>) {
        println("ListContacts command executed")
    }
}

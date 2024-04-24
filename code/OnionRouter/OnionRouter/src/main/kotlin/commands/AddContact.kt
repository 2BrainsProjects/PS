package commands

class AddContact : Command {
    override val id = 5

    override fun execute(args: List<String>) {
        println("AddCntact command executed")
    }
}

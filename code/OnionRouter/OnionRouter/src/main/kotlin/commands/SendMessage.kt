package commands

class SendMessage : Command {
    override val id = 7

    override fun execute(args: List<String>) {
        println("sendMessage command executed")
    }
}

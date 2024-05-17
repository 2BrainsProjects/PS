package commands

import LocalMemory

class SendMessage(private val sendMsg: (Int, String) -> Unit, localMemory: LocalMemory) : Command {
    override fun execute(args: List<String>) {
        TODO()
    }
}

package commands

fun interface Command {
    fun execute(args: List<String>)
}

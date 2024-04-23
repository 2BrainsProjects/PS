interface Command {
    val id: Int

    fun execute(args: List<String>)
}

package domain

data class Message(val conversationId: String, val content: String, val timestamp: String)

fun rgbfg(r:Int,g:Int,b:Int) = "\u001b[38;2;$r;$g;${b}m"

const val RC = "\u001b[0m"
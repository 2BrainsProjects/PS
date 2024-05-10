package pt.isel.ps.anonichat.domain.user

data class Message(val userId: Int, val cid: String, val message: String, val msgDate: String)
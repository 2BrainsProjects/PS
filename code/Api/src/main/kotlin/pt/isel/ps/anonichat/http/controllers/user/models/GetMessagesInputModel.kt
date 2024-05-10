package pt.isel.ps.anonichat.http.controllers.user.models

data class GetMessagesInputModel(val userId: Int, val cid: String, val token: String, val msgDate: String)
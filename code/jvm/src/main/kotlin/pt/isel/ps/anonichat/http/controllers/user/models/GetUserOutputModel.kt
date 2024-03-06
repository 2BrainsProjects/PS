package pt.isel.ps.anonichat.http.controllers.user.models

data class GetUserOutputModel(
    val id: Int,
    val ip: String,
    val name: String,
    val certificate: String
)

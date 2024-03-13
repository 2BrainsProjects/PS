package pt.isel.ps.anonichat.http.controllers.user.models

data class LoginOutputModel(
    val certificate: String,
    val token: String,
    val expiresIn: Long
)

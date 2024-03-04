package pt.isel.ps.anonichat.http.controllers.user.models

data class LoginOutputModel(
    val token: String,
    val expiresIn: Long
)

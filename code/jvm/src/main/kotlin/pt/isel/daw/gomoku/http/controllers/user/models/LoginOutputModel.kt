package pt.isel.daw.gomoku.http.controllers.user.models

data class LoginOutputModel(
    val token: String,
    val expiresIn: Long
)

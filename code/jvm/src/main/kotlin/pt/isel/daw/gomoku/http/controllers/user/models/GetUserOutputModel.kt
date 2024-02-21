package pt.isel.daw.gomoku.http.controllers.user.models

import pt.isel.daw.gomoku.domain.user.Stats

data class GetUserOutputModel(
    val name: String,
    val email: String,
    val stats: Stats
)

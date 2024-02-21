package pt.isel.daw.gomoku.http.pipeline.authentication

import pt.isel.daw.gomoku.domain.user.User

/**
 * Represents a user session (authenticated user)
 * @property user the user
 * @property token the user's token
 */
data class Session(val user: User, val token: String)

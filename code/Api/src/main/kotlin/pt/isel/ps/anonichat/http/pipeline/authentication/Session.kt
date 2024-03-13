package pt.isel.ps.anonichat.http.pipeline.authentication

import pt.isel.ps.anonichat.domain.user.User

/**
 * Represents a user session (authenticated user)
 * @property user the user
 * @property token the user's token
 */
data class Session(val user: User, val token: String)

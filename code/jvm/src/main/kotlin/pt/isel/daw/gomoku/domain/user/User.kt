package pt.isel.daw.gomoku.domain.user

/**
 * Represents a user in the system
 * @property id The user's id
 * @property name The user's name
 * @property email The user's email
 * @property passwordHash The user's password hash
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String
)

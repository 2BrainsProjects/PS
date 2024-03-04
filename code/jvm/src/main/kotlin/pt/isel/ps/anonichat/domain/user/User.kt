package pt.isel.ps.anonichat.domain.user

/**
 * Represents a user in the system
 * @property id The user's id
 * @property ip The user's ip
 * @property name The user's name
 * @property email The user's email
 * @property passwordHash The user's password hash
 * @property certificate The user's certificate path
 */
data class User(
    val id: Int,
    val ip: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val certificate: String
)

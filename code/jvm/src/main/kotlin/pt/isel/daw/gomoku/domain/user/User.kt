package pt.isel.daw.gomoku.domain.user

import org.jdbi.v3.core.mapper.Nested

/**
 * Represents a user in the system
 * @property id The user's id
 * @property name The user's name
 * @property email The user's email
 * @property passwordHash The user's password hash
 * @property stats The user's statistics
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    @Nested val stats: Stats
)

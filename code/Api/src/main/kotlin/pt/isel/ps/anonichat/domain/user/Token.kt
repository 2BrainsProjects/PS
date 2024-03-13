package pt.isel.ps.anonichat.domain.user

import kotlinx.datetime.Instant

/**
 * Represents a token
 * @property tokenHash The token hash
 * @property userId The user id
 * @property createdAt The creation date
 * @property lastUsedAt The last used date
 */
data class Token(
    val tokenHash: String,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant
)

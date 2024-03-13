package pt.isel.ps.anonichat.domain.user

import kotlin.time.Duration

/**
 * Represents the users domain configuration
 * @property tokenSizeInBytes The token size in bytes
 * @property tokenTtl The token time to live
 * @property tokenRollingTtl The token rolling time to live
 * @property maxTokensPerUser The maximum number of tokens per user
 */
data class UserDomainConfig(
    val tokenSizeInBytes: Int,
    val tokenTtl: Duration,
    val tokenRollingTtl: Duration,
    val maxTokensPerUser: Int
) {
    init {
        require(tokenSizeInBytes > 0)
        require(tokenTtl.isPositive())
        require(tokenRollingTtl.isPositive())
        require(maxTokensPerUser > 0)
    }
}

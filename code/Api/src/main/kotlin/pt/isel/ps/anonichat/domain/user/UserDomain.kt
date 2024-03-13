package pt.isel.ps.anonichat.domain.user

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import pt.isel.ps.anonichat.domain.user.utils.TokenEncoder
import java.security.SecureRandom
import java.util.Base64

/**
 * The domain of the users containing all the users logic
 */
@Component
class UserDomain(
    private val passwordEncoder: PasswordEncoder,
    private val tokenEncoder: TokenEncoder,
    private val config: UserDomainConfig
) {
    val maxTokensPerUser get() = config.maxTokensPerUser

    fun verifyPassword(password: String, hashedPassword: String) = passwordEncoder.matches(password, hashedPassword)

    fun encodePassword(password: String): String = passwordEncoder.encode(password)

    fun hashToken(token: String): String = tokenEncoder.hash(token)

    fun generateTokenValue(): String =
        ByteArray(config.tokenSizeInBytes).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    fun verifyToken(token: String, tokenHash: String) = tokenEncoder.matches(token, tokenHash)

    fun isToken(token: String): Boolean = try {
        Base64.getUrlDecoder().decode(token).size == config.tokenSizeInBytes
    } catch (ex: IllegalArgumentException) {
        false
    }

    fun getTokenExpiration(token: Token): Instant {
        val absoluteExpiration = token.createdAt + config.tokenTtl
        val rollingExpiration = token.lastUsedAt + config.tokenRollingTtl
        return minOf(absoluteExpiration, rollingExpiration)
    }

    fun hasTokenExpired(token: Token, clock: Clock): Boolean {
        val now = clock.now()
        val expirationTime = token.createdAt + config.tokenTtl
        val rollingExpirationTime = token.lastUsedAt + config.tokenRollingTtl
        return now.isAfter(expirationTime) || now.isAfter(rollingExpirationTime)
    }

    private fun Instant.isAfter(instant: Instant) = toJavaInstant().isAfter(instant.toJavaInstant())

    companion object {
        const val MIN_USERNAME_LENGTH = 3
        const val MAX_USERNAME_LENGTH = 20
        const val MIN_EMAIL_LENGTH = 3
        const val MAX_EMAIL_LENGTH = 30
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_LENGTH = 30
    }
}

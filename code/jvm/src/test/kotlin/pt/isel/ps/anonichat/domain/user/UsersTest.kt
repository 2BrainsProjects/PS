package pt.isel.ps.anonichat.domain.user

import kotlinx.datetime.Instant
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.ps.anonichat.domain.user.utils.Sha256TokenEncoder
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

class UsersTest {

    @Test
    fun `create a valid Password and validate it`() {
        // given: a password
        val password = "Password123!"

        // when: hashing the password
        val hashedPassword = domain.encodePassword(password)

        // then: the password is the same as the hashed password
        assertTrue(domain.verifyPassword(password, hashedPassword))
    }

    @Test
    fun `create a valid Token and validate it`() {
        // given: a token value
        val tokenValue = domain.generateTokenValue()

        // when: hashing the token value and creating a token
        val hashedToken = domain.hashToken(tokenValue)
        val token = Token(hashedToken, 1, Instant.fromEpochMilliseconds(12321), Instant.fromEpochMilliseconds(32131))

        // then: the token is valid and the token value is the same as token value hashed
        assertTrue(domain.isToken(tokenValue))
        assertTrue(domain.verifyToken(tokenValue, token.tokenHash))
    }

    companion object {
        private fun passwordEncoder() = BCryptPasswordEncoder()
        private fun tokenEncoder() = Sha256TokenEncoder()
        private val userDomainConfig = UserDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3
        )

        val domain = UserDomain(passwordEncoder(), tokenEncoder(), userDomainConfig)
    }
}

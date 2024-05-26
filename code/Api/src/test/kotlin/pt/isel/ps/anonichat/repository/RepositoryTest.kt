package pt.isel.ps.anonichat.repository

import pt.isel.ps.anonichat.AnonichatTest
import pt.isel.ps.anonichat.domain.user.utils.Sha256TokenEncoder
import pt.isel.ps.anonichat.repository.jdbi.JdbiMessageRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiRouterRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiTokenRepository
import pt.isel.ps.anonichat.repository.jdbi.JdbiUserRepository
import java.security.SecureRandom
import java.security.Timestamp
import java.util.*

open class RepositoryTest : AnonichatTest() {
    private val handle = jdbi.open()
    private val tokenEncoder = Sha256TokenEncoder()

    val usersRepository = JdbiUserRepository(handle)
    val tokenRepository = JdbiTokenRepository(handle)
    val routersRepository = JdbiRouterRepository(handle)
    val messagesRepository = JdbiMessageRepository(handle)

    fun registerTestUser(
        username: String = testUsername(),
        email: String = testEmail()
    ): Int {
        return usersRepository.registerUser(username, email, HASHED_TEST_PASSWORD)
    }

    fun registerTestRouter(
        ip: String = testIp()
    ): Int{
        return routersRepository.createRouter(ip)
    }

    fun hashToken(token: String): String = tokenEncoder.hash(token)

    fun generateToken(): String =
        ByteArray(TOKEN_LENGTH).let { byteArray ->
            SecureRandom.getInstanceStrong().nextBytes(byteArray)
            Base64.getUrlEncoder().encodeToString(byteArray)
        }

    companion object {
        const val HASHED_TEST_PASSWORD = "\$2a\$10\$Yf3CwWSnpLHhv2RfxoUDX.2BcMD6Yz9=GwnAspZyWhMTRyUC89E6i"
        const val MAX_TOKENS_PER_USER = 3
        private const val TOKEN_LENGTH = 32
    }
}

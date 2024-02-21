package pt.isel.daw.gomoku.repository

import pt.isel.daw.gomoku.GomokuTest
import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.user.utils.Sha256TokenEncoder
import pt.isel.daw.gomoku.repository.jdbi.JdbiGameRepository
import pt.isel.daw.gomoku.repository.jdbi.JdbiLobbyRepository
import pt.isel.daw.gomoku.repository.jdbi.JdbiTokenRepository
import pt.isel.daw.gomoku.repository.jdbi.JdbiUserRepository
import java.security.SecureRandom
import java.util.*

open class RepositoryTest : GomokuTest() {
    private val handle = jdbi.open()
    private val tokenEncoder = Sha256TokenEncoder()

    val usersRepository = JdbiUserRepository(handle)
    val lobbyRepository = JdbiLobbyRepository(handle)
    val gameRepository = JdbiGameRepository(handle)
    val tokenRepository = JdbiTokenRepository(handle)

    fun createAndGetTestGame(
        board: Board = Board(testBoardSize),
        user1: Int = registerTestUser(),
        user2: Int = registerTestUser()
    ): Game {
        return gameRepository.createGame(board, user1, user2, testGameConfig).let {
            gameRepository.getGame(it)
        }
    }

    fun registerTestUser(
        username: String = testUsername(),
        email: String = testEmail()
    ): Int {
        return usersRepository.registerUser(username, email, HASHED_TEST_PASSWORD)
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

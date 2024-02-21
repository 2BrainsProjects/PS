package pt.isel.daw.gomoku

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.gomoku.domain.game.GameConfig
import pt.isel.daw.gomoku.domain.game.Opening
import pt.isel.daw.gomoku.domain.game.Variant
import pt.isel.daw.gomoku.repository.jdbi.utils.configure
import java.util.*

open class GomokuTest {

    companion object {

        const val testBoardSize = 15
        const val initialRating = 600
        val testGameConfig = GameConfig(testBoardSize, Variant.FREESTYLE, Opening.FREESTYLE)

        fun testUsername() = "testUser${UUID.randomUUID().toString().substring(0, 6)}"
        fun testPassword() = "Password123!"
        fun testEmail() = "${testUsername()}@gmail.com"
        fun testUserData() = Triple(testUsername(), testEmail(), testPassword())

        val jdbi = Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            }
        ).configure()

        fun resetUsers() {
            jdbi.useHandle<Exception> { handle ->
                handle.execute("delete from dbo.User")
            }
        }

        fun resetGames() {
            jdbi.useHandle<Exception> { handle ->
                handle.execute("delete from dbo.Game")
            }
        }

        fun resetLobby() {
            jdbi.useHandle<Exception> { handle ->
                handle.execute("delete from dbo.Lobby")
            }
        }
    }
}

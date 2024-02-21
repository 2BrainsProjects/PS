package pt.isel.daw.gomoku.services

import kotlinx.datetime.Clock
import pt.isel.daw.gomoku.GomokuTest
import pt.isel.daw.gomoku.domain.game.GameDomain
import pt.isel.daw.gomoku.domain.lobby.LobbyDomain
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.domain.user.UsersTest
import pt.isel.daw.gomoku.repository.jdbi.transaction.JdbiTransactionManager

open class ServicesTest : GomokuTest() {
    val gameService = GameService(JdbiTransactionManager(jdbi), GameDomain())
    val usersServices = UserService(JdbiTransactionManager(jdbi), UsersTest.domain, Clock.System)
    val lobbyService = LobbyService(JdbiTransactionManager(jdbi), LobbyDomain())

    fun registerAndGetTestUser(): User {
        val (name, email, password) = testUserData()
        return usersServices.registerUser(name, email, password).let { userId ->
            val userModel = usersServices.getUser(userId)
            User(userId, userModel.name, userModel.email, password, userModel.stats)
        }
    }
}

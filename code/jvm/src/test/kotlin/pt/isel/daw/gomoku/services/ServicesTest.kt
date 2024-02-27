package pt.isel.daw.gomoku.services

import kotlinx.datetime.Clock
import pt.isel.daw.gomoku.GomokuTest
import pt.isel.daw.gomoku.domain.user.User
import pt.isel.daw.gomoku.domain.user.UsersTest
import pt.isel.daw.gomoku.repository.jdbi.transaction.JdbiTransactionManager

open class ServicesTest : GomokuTest() {
    val usersServices = UserService(JdbiTransactionManager(jdbi), UsersTest.domain, Clock.System)

    fun registerAndGetTestUser(): User {
        val (name, email, password) = testUserData()
        return usersServices.registerUser(name, email, password).let { userId ->
            val userModel = usersServices.getUser(userId)
            User(userId, userModel.name, userModel.email, password)
        }
    }
}

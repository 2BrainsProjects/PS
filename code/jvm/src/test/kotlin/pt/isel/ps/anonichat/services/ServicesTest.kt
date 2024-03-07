package pt.isel.ps.anonichat.services

import kotlinx.datetime.Clock
import pt.isel.ps.anonichat.GomokuTest
import pt.isel.ps.anonichat.domain.certificate.CertificateDomain
import pt.isel.ps.anonichat.domain.user.UsersTest
import pt.isel.ps.anonichat.repository.jdbi.transaction.JdbiTransactionManager
import java.io.BufferedReader
import java.io.FileInputStream

open class ServicesTest : GomokuTest() {
    val usersServices = UserService(JdbiTransactionManager(jdbi), UsersTest.domain, CertificateDomain(), Clock.System)

//    fun registerAndGetTestUser(): User {
//
//        val (name, email, password, clientCSR) = testUserData()
//        return usersServices.registerUser(name, email, password, clientCSR).let { (userId, certPath) ->
//            val userModel = usersServices.getUsers(listOf(userId))
//            User(userId, userModel.users, userModel.email, password)
//        }
//    }
}

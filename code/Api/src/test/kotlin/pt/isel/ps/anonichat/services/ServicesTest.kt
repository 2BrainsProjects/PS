package pt.isel.ps.anonichat.services

import kotlinx.datetime.Clock
import pt.isel.ps.anonichat.AnonichatTest
import pt.isel.ps.anonichat.domain.certificate.CertificateDomain
import pt.isel.ps.anonichat.domain.user.UsersTest
import pt.isel.ps.anonichat.repository.jdbi.transaction.JdbiTransactionManager

open class ServicesTest : AnonichatTest() {
    val usersServices = UserService(JdbiTransactionManager(jdbi), UsersTest.domain, CertificateDomain(), Clock.System)
    val routersServices = RouterService(JdbiTransactionManager(jdbi), CertificateDomain())

//    fun registerAndGetTestUser(): User {
//
//        val (name, email, password, clientCSR) = testUserData()
//        return usersServices.registerUser(name, email, password, clientCSR).let { (userId, certPath) ->
//            val userModel = usersServices.getUsers(listOf(userId))
//            User(userId, userModel.users, userModel.email, password)
//        }
//    }
}

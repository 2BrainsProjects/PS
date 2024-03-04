package pt.isel.ps.anonichat

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ps.anonichat.repository.jdbi.utils.configure
import java.util.*

open class GomokuTest {

    companion object {

        const val testBoardSize = 15
        const val initialRating = 600

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
    }
}

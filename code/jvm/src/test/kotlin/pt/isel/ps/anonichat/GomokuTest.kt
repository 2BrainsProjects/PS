package pt.isel.ps.anonichat

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ps.anonichat.repository.jdbi.utils.configure
import java.util.*

open class GomokuTest {
    data class UserTest(val username: String, val email: String, val password: String, val publicKey: String)

    companion object {

        fun testUsername() = "testUser${UUID.randomUUID().toString().substring(0, 6)}"
        fun testPassword() = "Password123!"
        fun testEmail() = "${testUsername()}@gmail.com"
        private fun testPublicKey() = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvLgU8RYbHDA3SXpyPl6WyG64j1rtFc/9lgFkKMz8xBBI58xgTM/vw3JXWCb1QUOIHHaa1y+LJmgmaT+Ll6DVgoNstKGMatjdDLphDOy5RI4/L7on2NQHTF6FOCwYcpSIc5/I9vIF1YTEuncrHaP/R5kia11tYOd+WV3a/x8llODkn+Jd0dZGv7tWNaskdIbCQy3DBCHC5UvLuRpjYR/AGeLq+/PvQS9zpirybjU8ev6JVAoDdQnPNnG/HcHxJDk/Xgay89qkomUh/g94C+zTrdfpRYj27L5EDfsWLkU4tvYSnblqKzjyZwvRNNmy0OvDFcOqvlep23MWkQhokPHxowIDAQAB"
        fun testUserData() = UserTest(
            testUsername(),
            testEmail(),
            testPassword(),
            testPublicKey()
        )

        fun testIp(): String{
            val random = Random()
            return "${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}.${random.nextInt(256)}"
        }

        fun testCertificate(): String {
            val certContent = UUID.randomUUID().toString()
            return "-----BEGIN CERTIFICATE-----\n" +
                    certContent + "\n" +
                    "-----END CERTIFICATE-----"
            }

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

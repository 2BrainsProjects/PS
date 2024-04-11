package pt.isel.ps.anonichat

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.ps.anonichat.repository.jdbi.utils.configure
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.*

open class AnonichatTest {
    data class UserTest(val username: String, val email: String, val password: String, val publicKey: String)

    companion object {
        val basePath
            get() = pathBuilder()
        const val USERS = "\\users"
        const val ROUTERS = "\\routers"

        private fun pathBuilder() = System.getProperty("user.dir") + "\\src\\test\\kotlin\\pt\\isel\\ps\\anonichat\\certificates"

        fun testUsername() = "testUser${UUID.randomUUID().toString().substring(0, 6)}"
        fun testPassword() = "Password123!"
        fun testEmail() = "${testUsername()}@gmail.com"
        fun testUserCSR() = generateClientCSR(generateRandomId(), testUsername(), testEmail(), testPassword())
        private fun testRouterCSR(ip: String, pwd: String) = generateRouterCSR(generateRandomId(), ip, pwd)
        private fun generateRandomId() = Random().nextInt(Int.MAX_VALUE)
        fun testUserData() = UserTest(
            testUsername(),
            testEmail(),
            testPassword(),
            testUserCSR()
        )

        fun testRouterData(): Triple<String, String, String> {
            val ip = testIp()
            val pwd = testPassword()
            return Triple(ip, testRouterCSR(ip, pwd), pwd)
        }

        private fun generateCSR(id: Int, pseudoname: String, email: String, pwd: String, extraPath: String): String {
            answeringCSRCreation(id, pseudoname, email, pwd, extraPath)
            BufferedReader(InputStreamReader(FileInputStream("$basePath$extraPath/$id.csr"))).use {
                return it.readLines().drop(1).dropLast(1).joinToString("")
            }
        }

        private fun generateClientCSR(userId: Int, username: String, email: String, pwd: String): String =
            generateCSR(userId, username, email, pwd, USERS)

        private fun generateRouterCSR(userId: Int, username: String, pwd: String): String =
            generateCSR(userId, username, "", pwd, ROUTERS)

        private fun answeringCSRCreation(id: Int, name: String, email: String, password: String, extraPath: String) {
            val command =
                "openssl req -out $basePath$extraPath/$id.csr -new -newkey rsa:2048 -nodes -keyout $basePath$extraPath/$id.key"
            try {
                val process = ProcessBuilder(command.split(" "))
                    .redirectErrorStream(true)
                    .start()

                // Provide input to the process
                val writer = BufferedWriter(OutputStreamWriter(process.outputStream))
                repeat(5){
                    writer.write("\n")
                    writer.flush()
                }
                writer.write("$name\n")
                writer.flush()
                writer.write("$email\n")
                writer.flush()
                writer.write("$password\n")
                writer.flush()
                writer.write("\n")
                writer.flush()
                writer.close()

                process.waitFor()
            }catch (e: Exception){
                throw IllegalStateException(e.message)
            }
        }

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

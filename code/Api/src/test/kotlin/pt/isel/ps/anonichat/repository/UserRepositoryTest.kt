package pt.isel.ps.anonichat.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UserRepositoryTest : RepositoryTest() {

    @Test
    fun `user created and retrieved successfully`() {
        // given: a username, email and password
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD

        // when: registering a new user
        val userId = usersRepository.registerUser(username, email, password)

        // then: the user is created
        val user = usersRepository.getUser(userId)
        assertEquals(userId, user.id)
        assertEquals(username, user.name)
        assertEquals(email, user.email)
        assertEquals(password, user.passwordHash)

        // and: the user can be retrieved by username or email
        assertTrue(usersRepository.isUser(userId))
        assertTrue(usersRepository.isUserByUsername(username))
        assertTrue(usersRepository.isUserByEmail(email))
        assertEquals(user, usersRepository.getUserByUsername(username))
        assertEquals(user, usersRepository.getUserByEmail(email))
    }

    @Test
    fun `update the ip of the user`() {
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD
        val ip = testIp()

        val userId = usersRepository.registerUser(username, email, password)

        val userAfterCreation = usersRepository.getUser(userId)

        assertEquals(userAfterCreation.ip, null)

        val flag = usersRepository.updateIp(userId, ip)
        val userAfterUpdate = usersRepository.getUser(userId)

        assertTrue(flag)
        assertEquals(userAfterUpdate.ip, ip)
    }

    @Test
    fun `update the certificate of the user`() {
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD

        val userId = usersRepository.registerUser(username, email, password)

        val cert = "/certificate/$userId"

        val userAfterCreation = usersRepository.getUser(userId)

        assertEquals(userAfterCreation.certificate, null)

        val flag = usersRepository.updateCert(userId, cert)
        val userAfterUpdate = usersRepository.getUser(userId)

        assertTrue(flag)
        assertEquals(userAfterUpdate.certificate, cert)
    }

    @Test
    fun `get the last user id`() {
        val totalUsers = usersRepository.getTotalUsers()
        val lastId = usersRepository.getLastId()

        assertTrue(totalUsers <= lastId)
    }

    @Test
    fun `update and get user session`() {
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD
        val sessionInfo = "123"

        val userId = usersRepository.registerUser(username, email, password)

        assertTrue(usersRepository.updateSessionInfo(userId, sessionInfo))

        val session = usersRepository.getUserSession(userId)
        assertEquals(sessionInfo, session)
    }
}

package pt.isel.ps.anonichat.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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
    fun `save and get message`(){
        val username = testUsername()
        val email = testEmail()
        val password = HASHED_TEST_PASSWORD

        val userId = usersRepository.registerUser(username, email, password)
        val cid = testCid()
        val message1 = "hello, how you doing?"
        val message2 = "Well and you?"
        val msgDate1 = testTimestamp()
        assertTrue(usersRepository.saveMessages(userId, cid, message1, msgDate1))
        Thread.sleep(1000)

        val msgDate2 = testTimestamp()
        usersRepository.saveMessages(userId, cid, message2, msgDate2)

        val messages = usersRepository.getMessages(userId, cid)
        assertEquals(2, messages.size)

        val msg = messages.firstOrNull{it.message == message2}
        assertNotNull(msg)
        assertEquals(message2, msg.message)
        assertEquals(userId, msg.userId)
        assertEquals(cid, msg.cid)
        assertEquals(msgDate2, msg.msgDate)
        assertNotNull(messages.firstOrNull{it.message == message1})

        val messagesWithTime = usersRepository.getMessages(userId, cid, msgDate1)

        assertEquals(1, messagesWithTime.size)
        val msgWithTime = messagesWithTime.first()
        assertEquals(message2, msgWithTime.message)
        assertEquals(userId, msgWithTime.userId)
        assertEquals(cid, msgWithTime.cid)
        assertEquals(msgDate2, msgWithTime.msgDate)
    }

    @Test
    fun `update and get user session`(){
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

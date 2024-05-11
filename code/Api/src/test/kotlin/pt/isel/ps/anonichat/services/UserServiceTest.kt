package pt.isel.ps.anonichat.services

import pt.isel.ps.anonichat.domain.exceptions.UserException.UserNotFoundException
import pt.isel.ps.anonichat.domain.exceptions.UserException.UserAlreadyExistsException
import kotlin.math.min
import kotlin.test.*

class UserServiceTest : ServicesTest() {

    private val path = basePath + USERS
    @Test
    fun `register a user`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val userId = usersServices.registerUser(name, email, password, clientCSR, path)

        // then: the user is registered
        val users = usersServices.getUsers(listOf(userId))
        assertEquals(name, users.users[0].name)
        assertEquals(email, users.users[0].email)
        assertTrue(users.users[0].certificate.isNotEmpty())

        // when: registering the same user again
        // then: an exception is thrown
        assertFailsWith<UserAlreadyExistsException> {
            usersServices.registerUser(name, email, password, clientCSR, path)
        }
    }

    @Test
    fun `login a user by username`() {
        // given: a user
        val path = basePath + "\\$USERS"
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password, clientCSR, path)

        // and: logging in the user
        val (token) = usersServices.loginUser(name, null, password, "192.127.0.1", path)

        // and: getting the user by token
        val userByToken = usersServices.getUserByToken(token.value)

        // then: the user is logged in
        assertEquals(name, userByToken?.name)
        assertEquals(email, userByToken?.email)
    }

    @Test
    fun `login a user by email`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()
        // when: registering the user
        usersServices.registerUser(name, email, password, clientCSR, path)
        // and: logging in the user by email
        val (token) = usersServices.loginUser(null, email, password, "192.127.0.1", path)
        // and: getting the user by token
        val userByToken = usersServices.getUserByToken(token.value)
        // then: the user is logged in
        assertEquals(name, userByToken?.name)
        assertEquals(email, userByToken?.email)
    }

    @Test
    fun `get users`() {

        val ids = emptyList<Int>().toMutableList()
        for (i in 0..3) {
            // given: a user
            val (name, email, password, clientCSR) = testUserData()
            // when: registering the user
            val userId = usersServices.registerUser(name, email, password, clientCSR, path)
            ids.add(userId)
        }

        val maxId = usersServices.getLastId()

        // then: when getting the users, the user is present
        ids.add(maxId+1)
        val (users) = usersServices.getUsers(ids)
        ids.remove(maxId+1)
        assertEquals(ids.size, users.size)
        assertTrue(users.map { it.id }.containsAll(ids))
    }

    @Test
    fun `logout a user`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password, clientCSR, path)

        // and: logging in the user
        val (token, _) = usersServices.loginUser(name, null, password, "192.127.0.1", path)

        // then: the user is logged in
        val userByToken = usersServices.getUserByToken(token.value)
        assertNotNull(userByToken)
        assertEquals(name, userByToken.name)

        // when: revoking the token
        usersServices.revokeToken(token.value)

        // then: the token is no longer valid to get the user
        assertEquals(null, usersServices.getUserByToken(token.value))
    }

    @Test
    fun `save and get messages`(){
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val userId = usersServices.registerUser(name, email, password, clientCSR, path)

        val cid = testCid()
        val msgDate = testTimestamp()
        val msgs = listOf("hello, how you doing?", "well and you?")
        assertTrue(usersServices.saveMessages(userId, cid, msgs.first(), msgDate))
        Thread.sleep(1000)
        assertTrue(usersServices.saveMessages(userId, cid, msgs.last(), testTimestamp()))

        val messages = usersServices.getMessages(userId, cid, null)
        assertEquals(2, messages.size)
        assertEquals(2, messages.map { if (msgs.contains(it.message)) it.message}.size)

        val messagesWithTime = usersServices.getMessages(userId, cid, msgDate)
        assertEquals(1, messagesWithTime.size)
        assertEquals(messagesWithTime.first().message, msgs.last())
    }

    @Test
    fun `try to save and get messages with invalid user`(){
        val cid = testCid()
        // when: registering the user
        val invalidId = usersServices.getLastId() + 1

        assertFailsWith<UserNotFoundException> { usersServices.saveMessages(invalidId, cid, "oi", testTimestamp()) }
        assertFailsWith<UserNotFoundException> { usersServices.getMessages(invalidId, cid, null) }
    }

    @Test
    fun `save and get sessionInfo`(){
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val userId = usersServices.registerUser(name, email, password, clientCSR, path)
        val sessionInfo = "1234"
        usersServices.saveSessionInfo(userId, sessionInfo)

        val (_, session) = usersServices.loginUser(name, null, password, testIp(), path)
        assertEquals(sessionInfo, session)


    }
}

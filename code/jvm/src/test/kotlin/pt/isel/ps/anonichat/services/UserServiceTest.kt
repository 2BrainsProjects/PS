package pt.isel.ps.anonichat.services

import org.junit.jupiter.api.RepeatedTest
import org.springframework.test.annotation.Repeat
import pt.isel.ps.anonichat.domain.exceptions.UserException.UserAlreadyExistsException
import kotlin.math.min
import kotlin.test.*

class UserServiceTest : ServicesTest() {

    @Test
    fun `register a user`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val (userId, _) = usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")

        // then: the user is registered
        val users = usersServices.getUsers(listOf(userId))
        assertEquals(name, users.users[0].name)
        assertEquals(email, users.users[0].email)

        // when: registering the same user again
        // then: an exception is thrown
        assertFailsWith<UserAlreadyExistsException> {
            usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")
        }
    }

    @Test
    fun `login a user by username`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")

        // and: logging in the user
        val (token) = usersServices.loginUser(name, null, password, "192.127.0.1", basePath + "\\$USERS")

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
        usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")

        // and: logging in the user by email
        val (token) = usersServices.loginUser(null, email, password, "192.127.0.1", basePath + "\\$USERS")

        // and: getting the user by token
        val userByToken = usersServices.getUserByToken(token.value)

        // then: the user is logged in
        assertEquals(name, userByToken?.name)
        assertEquals(email, userByToken?.email)
    }

    @Test
    fun `get users`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val (userId) = usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")

        val maxId = usersServices.getLastId()

        // then: when getting the users, the user is present
        val list = List(min(5, maxId)){
            if(it == 0)
                userId
            else
                (0..maxId).random()
        }.distinct()
        val (users, totalUsers) = usersServices.getUsers(list)
        assertTrue(totalUsers > 0)
        assertTrue(users.isNotEmpty())
    }

    @Test
    fun `logout a user`() {
        // given: a user
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password, clientCSR, basePath + "\\$USERS")

        // and: logging in the user
        val (token, _) = usersServices.loginUser(name, null, password, "192.127.0.1", basePath + "\\$USERS")

        // then: the user is logged in
        val userByToken = usersServices.getUserByToken(token.value)
        assertNotNull(userByToken)
        assertEquals(name, userByToken.name)

        // when: revoking the token
        usersServices.revokeToken(token.value)

        // then: the token is no longer valid to get the user
        assertEquals(null, usersServices.getUserByToken(token.value))
    }
}

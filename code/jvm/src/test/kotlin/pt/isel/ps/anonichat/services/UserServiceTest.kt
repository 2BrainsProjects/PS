package pt.isel.ps.anonichat.services

import pt.isel.ps.anonichat.domain.exceptions.UserException.UserAlreadyExistsException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class UserServiceTest : ServicesTest() {

    @Test
    fun `register a user`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        val userId = usersServices.registerUser(name, email, password)

        // then: the user is registered
        val user = usersServices.getUser(userId)
        assertEquals(name, user.name)
        assertEquals(email, user.email)

        // when: registering the same user again
        // then: an exception is thrown
        assertFailsWith<UserAlreadyExistsException> {
            usersServices.registerUser(name, email, password)
        }
    }

    @Test
    fun `login a user by username`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password)

        // and: logging in the user
        val (token) = usersServices.loginUser(name, null, password)

        // and: getting the user by token
        val userByToken = usersServices.getUserByToken(token)

        // then: the user is logged in
        assertEquals(name, userByToken?.name)
        assertEquals(email, userByToken?.email)
    }

    @Test
    fun `login a user by email`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password)

        // and: logging in the user by email
        val (token) = usersServices.loginUser(null, email, password)

        // and: getting the user by token
        val userByToken = usersServices.getUserByToken(token)

        // then: the user is logged in
        assertEquals(name, userByToken?.name)
        assertEquals(email, userByToken?.email)
    }

    @Test
    fun `get user by id`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        val userId = usersServices.registerUser(name, email, password)

        // and: getting the user by id
        val userById = usersServices.getUser(userId)

        // then: the user is the same as the one registered
        val user = usersServices.getUser(userId)
        assertEquals(user.name, userById.name)
        assertEquals(user.email, userById.email)
    }

    @Test
    fun `get users`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password)

        // then: when getting the users, the user is present
        val (users, totalUsers) = usersServices.getUsers(0, 5, "rating", "asc")
        assertTrue(totalUsers > 0)
        assertTrue(users.isNotEmpty())
    }

    @Test
    fun `logout a user`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        usersServices.registerUser(name, email, password)

        // and: logging in the user
        val (token) = usersServices.loginUser(name, null, password)

        // then: the user is logged in
        val userByToken = usersServices.getUserByToken(token)
        assertEquals(name, userByToken?.name)

        // when: revoking the token
        usersServices.revokeToken(token)

        // then: the token is no longer valid to get the user
        assertEquals(null, usersServices.getUserByToken(token))
    }
}

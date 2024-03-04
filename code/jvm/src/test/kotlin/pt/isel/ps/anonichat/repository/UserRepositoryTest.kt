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
    fun `can get list of users`() {
        // given: test users
        registerTestUser()
        registerTestUser()
        registerTestUser()

        // when: getting the first 3 users ordered by id ascending
        val limit = 3
        val users = usersRepository.getUsers(0, limit, "id", "asc")

        // then: we get the first 3 oldest users in the database
        assertTrue(users.isNotEmpty())
        assertTrue(users.all { it.id > 0 })
        assertEquals(limit, users.size)
        assertEquals(users.sortedBy { it.id }, users)
    }
}

package pt.isel.daw.gomoku.http

import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.gomoku.domain.user.Stats
import pt.isel.daw.gomoku.http.controllers.user.models.GetUserOutputModel
import pt.isel.daw.gomoku.http.controllers.user.models.GetUsersCountOutputModel
import pt.isel.daw.gomoku.http.controllers.user.models.LoginOutputModel
import pt.isel.daw.gomoku.http.hypermedia.SirenEntity
import pt.isel.daw.gomoku.http.hypermedia.SirenEntityEmbeddedLinkModel
import pt.isel.daw.gomoku.http.hypermedia.SirenEntityEmbeddedRepresentationModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserControllerTest : HttpTest() {

    @Test
    fun `can create a user`() {
        // given: user data
        val (name, email, password) = testUserData()

        // when: registering the user
        val userLink = client.post().uri(api("/register"))
            .bodyValue(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/user"))
            }
            .expectBody<SirenEntityEmbeddedLinkModel<Unit>>()
            .returnResult().responseHeaders.location?.path

        // then: the user link is not null
        assertNotNull(userLink)

        // when: getting the user with the link obtained
        // then: the response is the user expected
        val user = getUserHttp(userLink).properties!!
        assertEquals(name, user.name)
        assertEquals(email, user.email)
    }

    @Test
    fun `can create a user, obtain a token, access user home and logout`() {
        // given: a user
        val (name, email, password) = testUserData()
        registerTestUserHttp(name, email, password)

        // when: creating a token
        // then: the response is a 200
        val (token) = client.post().uri(api("/login"))
            .bodyValue(
                mapOf(
                    "name" to name,
                    "password" to password
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<LoginOutputModel>>()
            .returnResult().responseBody?.properties!!

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        client.get().uri(api("/me"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // when: getting the user home with an invalid token
        // then: the response is a 401 with the proper problem
        client.get().uri(api("/me"))
            .header("Authorization", "Bearer 123456789")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: revoking the token
        // then: response is a 200
        client.post().uri(api("/logout"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk

        // when: getting the user home with the revoked token
        // then: response is a 401
        client.get().uri(api("/me"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("WWW-Authenticate", "bearer")
            .expectHeader().valueEquals("Content-Type", "application/problem+json")
    }

    @Test
    fun `can get a user`() {
        // given: a user
        val (name, email, password) = testUserData()
        val userLink = registerTestUserHttp(name, email, password)

        // when: getting the user data
        val user = client.get().uri(api(userLink))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<GetUserOutputModel>>()
            .returnResult().responseBody?.properties!!

        // then: the user data is the same as the one used to create the user
        assertEquals(name, user.name)
        assertEquals(email, user.email)
        assertEquals(Stats(600, 0, 0, 0, 0), user.stats)
    }

    @Test
    fun `can get 2 users ordered by name in descending order`() {
        // given: 3 users
        registerTestUserHttp()
        registerTestUserHttp()
        registerTestUserHttp()

        val limit = 3
        // when: getting 3 users ordered by name in descending order
        val response = client.get().uri(api("/users?limit=$limit&orderBy=name&sort=desc"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetUsersCountOutputModel>>()
            .returnResult().responseBody

        val usersCount = response?.properties?.count!!
        val users = response.entities!!.map { entity ->
            getUserOutputModel(entity.properties as LinkedHashMap<*, *>)
        }

        // then: gets the 2 users ordered by rating desc
        val usernames = users.map { it.name.lowercase() }
        assertEquals(usersCount, users.size)
        assertEquals(limit, usersCount)
        assertEquals(usernames.sortedDescending(), usernames)
    }

    private fun getUserOutputModel(map: LinkedHashMap<*, *>): GetUserOutputModel {
        val name = map["name"] as String
        val email = map["email"] as String
        val stats = map["stats"] as LinkedHashMap<*, *>
        val rating = stats["rating"] as Int
        val gamesPlayed = stats["gamesPlayed"] as Int
        val wins = stats["wins"] as Int
        val losses = stats["losses"] as Int
        val draws = stats["draws"] as Int
        return GetUserOutputModel(name, email, Stats(rating, gamesPlayed, wins, draws, losses))
    }
}

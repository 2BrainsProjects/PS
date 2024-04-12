package pt.isel.ps.anonichat.http

 import org.springframework.test.web.reactive.server.expectBody
 import pt.isel.ps.anonichat.http.controllers.user.models.GetUserOutputModel
 import pt.isel.ps.anonichat.http.controllers.user.models.GetUsersCountOutputModel
 import pt.isel.ps.anonichat.http.controllers.user.models.LoginOutputModel
 import pt.isel.ps.anonichat.http.controllers.user.models.RegisterOutputModel
 import pt.isel.ps.anonichat.http.hypermedia.SirenEntity
 import pt.isel.ps.anonichat.http.hypermedia.SirenEntityEmbeddedLinkModel
 import pt.isel.ps.anonichat.http.hypermedia.SirenEntityEmbeddedRepresentationModel
 import kotlin.test.Test
 import kotlin.test.assertEquals
 import kotlin.test.assertNotNull
 import kotlin.test.assertTrue

class UserControllerTest : HttpTest() {

    @Test
    fun `can create a user`() {
        // given: user data
        val (name, email, password, clientCSR) = testUserData()

        // when: registering the user
        val userInfo = client.post().uri(api("/register"))
            .bodyValue(
                mapOf(
                    "name" to name,
                    "email" to email,
                    "password" to password,
                    "clientCSR" to clientCSR
                )
            )
            .exchange()
            .expectStatus().isCreated
            .expectHeader().value("location") {
                assertTrue(it.startsWith("/me"))
            }
            .expectBody<SirenEntityEmbeddedLinkModel<RegisterOutputModel>>()
            .returnResult().responseBody?.properties

        // then: the user info is not null
        assertNotNull(userInfo)

        // when: getting the user with the link obtained
        val userInHashMap = getUsersHttp(listOf(userInfo.userId))?.entities?.first()?.properties as LinkedHashMap<*, *>
        val user = getUserOutputModel(userInHashMap)

        // then: the response is the user expected
        assertEquals(name, user.name)
    }

    @Test
    fun `can create a user, obtain a token, access user home and logout`() {
        // given: a user
        val (name, email, password, _, ip) = testUserData()
        registerTestUserHttp(name, email, password)

        // when: creating a token
        // then: the response is a 200
        val (_, token, _) = client.post().uri(api("/login"))
            .bodyValue(
                mapOf(
                    "name" to name,
                    "password" to password,
                    "ip" to ip
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
    fun `can get 3 users`() {
        // given: 5 users
        val (u1Id, _) = registerTestUserHttp()
        val (u2Id, _) = registerTestUserHttp()
        val (u3Id, _) = registerTestUserHttp()
        val (u4Id, _) = registerTestUserHttp()
        val (u5Id, _) = registerTestUserHttp()

        val maxIdResponse = client.get().uri(api("/users/count"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetUsersCountOutputModel>>()
            .returnResult().responseBody?.properties?.maxId

        assertNotNull(maxIdResponse)

        assertTrue(maxIdResponse >= u5Id)

        val limit = 3
        val chosenIds = listOf(u2Id, u3Id, u5Id)
        // when: getting 3 users ordered by name in descending order
        val response = client.get().uri(api("/users?ids=${chosenIds.joinToString(",")}"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetUsersCountOutputModel>>()
            .returnResult().responseBody

        //val usersCount = response?.properties?.count!!
        val users = response?.entities!!.map { entity ->
            getUserOutputModel(entity.properties as LinkedHashMap<*, *>)
        }

        // then: gets the 3 requested users
        assertEquals(limit, users.size)
        chosenIds.forEach { id ->
            assertTrue(users.any { it.id == id })
        }
    }

    private fun getUserOutputModel(map: LinkedHashMap<*, *>): GetUserOutputModel {
        return GetUserOutputModel(
            map["id"] as Int,
            map["ip"] as String,
            map["name"] as String,
            map["certificate"] as String
        )
    }
}

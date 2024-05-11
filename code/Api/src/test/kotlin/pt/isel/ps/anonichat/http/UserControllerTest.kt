package pt.isel.ps.anonichat.http

 import org.springframework.http.MediaType
 import org.springframework.test.web.reactive.server.expectBody
 import org.springframework.util.LinkedMultiValueMap
 import org.springframework.util.MultiValueMap
 import org.springframework.web.reactive.function.BodyInserters
 import pt.isel.ps.anonichat.http.controllers.user.models.*
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
        val body: MultiValueMap<String, String> = LinkedMultiValueMap()
        body.add("name", name)
        body.add("email", email)
        body.add("password", password)
        body.add("clientCSR", clientCSR)

        val userInfo = client.post().uri(api("/users"))
            .body(
                BodyInserters.fromFormData(body)
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
    fun `can create a user, obtain a token and sessionInfo, access user home and logout`() {
        // given: a user
        val (name, email, password, _, ip) = testUserData()
        registerTestUserHttp(name, email, password)

        // when: creating a token
        // then: the response is a 200
        val bodyLogin: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyLogin.add("name", name)
        bodyLogin.add("password", password)
        bodyLogin.add("ip", ip)

        val (token, _, _) = client.post().uri(api("/login"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(bodyLogin))
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

        val session = "123"

        val bodyLogout: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyLogout.add("sessionInfo", session)

        // when: revoking the token
        // then: response is a 200
        client.post().uri(api("/logout"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("Authorization", "Bearer $token")
            .body(BodyInserters.fromFormData(bodyLogout))
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

        val (_, _, sessionInfo) = client.post().uri(api("/login"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(bodyLogin))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<LoginOutputModel>>()
            .returnResult().responseBody?.properties!!

        assertEquals(session, sessionInfo)
    }

    @Test
    fun `can get 3 users`() {
        // given: 5 users
        val (u1Id) = registerTestUserHttp()
        val (u2Id) = registerTestUserHttp()
        val (u3Id) = registerTestUserHttp()
        val (u4Id) = registerTestUserHttp()
        val (u5Id) = registerTestUserHttp()

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

    @Test
    fun `can get and save messages`(){
        val (name, email, password, _, ip) = testUserData()
        registerTestUserHttp(name, email, password)
        val cid = testCid()
        val msgs = listOf("msg1", "msg2", "msg3")
        val msgDateList = List(msgs.size) {
            Thread.sleep(1000)
            testTimestamp()
        }

        val bodyLogin: MultiValueMap<String, String> = LinkedMultiValueMap()
        bodyLogin.add("name", name)
        bodyLogin.add("password", password)
        bodyLogin.add("ip", ip)

        val (token, _, _) = client.post().uri(api("/login"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(bodyLogin))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntity<LoginOutputModel>>()
            .returnResult().responseBody?.properties!!
        for (i in msgs.indices) {
            val bodyPost: MultiValueMap<String, String> = LinkedMultiValueMap()
            bodyPost.add("cid", cid)
            bodyPost.add("message", msgs[i])
            bodyPost.add("msgDate", msgDateList[i])

            client.post().uri(api("/messages"))
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(bodyPost))
                .exchange()
                .expectStatus().isOk
        }

        val response = client.get().uri(api("/messages?cid=$cid&msgDate=${msgDateList.first()}"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetMessagesOutputModel>>()
            .returnResult().responseBody

        val messages = response?.entities!!.map { entity ->
            getMessageOutputModel(entity.properties as LinkedHashMap<*, *>)
        }
        assertNotNull(messages)
        assertEquals(2, messages.size)
        assertNotNull(messages.firstOrNull() { it.message == msgs[1] })
        assertNotNull(messages.firstOrNull() { it.message == msgs[2] })

    }

    private fun getMessageOutputModel(map: LinkedHashMap<*, *>): GetMessageOutputModel {
        return GetMessageOutputModel(
            map["cid"] as String,
            map["message"] as String,
            map["msgDate"] as String,
        )
    }
    private fun getUserOutputModel(map: LinkedHashMap<*, *>): GetUserInformationOutputModel {
        return GetUserInformationOutputModel(
            map["id"] as Int,
            map["ip"] as String,
            map["name"] as String,
            map["certificate"] as String
        )
    }
}

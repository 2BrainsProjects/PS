package pt.isel.daw.gomoku.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LobbyControllerTest : HttpTest() {

    @Test
    fun `user joins lobby`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        registerTestUserHttp(name, email, password)

        // and: logging in the user
        val token = loginTestUserHttp(name, password)

        resetLobby()

        // and: the user joins the lobby
        val boardSize = 15
        val gameLink = joinLobbyHttp(token, boardSize)

        // then: the response is a 200
        assertNull(gameLink)

        // when: the user tries to join the lobby again
        // then: the response is a 400
        client.post().uri(api("/lobby/join"))
            .header("Authorization", "Bearer $token")
            .bodyValue(
                mapOf(
                    "boardSize" to boardSize
                )
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: the user tries to join the lobby without a token
        // then: the response is a 401
        client.post().uri(api("/lobby/join"))
            .bodyValue(
                mapOf(
                    "boardSize" to boardSize
                )
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: the user tries to join the lobby with a wrong token
        // then: the response is a 401
        client.post().uri(api("/lobby/join"))
            .header("Authorization", "Bearer 1234124512")
            .bodyValue(
                mapOf(
                    "boardSize" to boardSize
                )
            )
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")
    }

    @Test
    fun `user leaves lobby`() {
        // given: a user
        val (name, email, password) = testUserData()

        // when: registering the user
        registerTestUserHttp(name, email, password)

        // and: logging in the user
        val token = loginTestUserHttp(name, password)

        resetLobby()

        // and: the user joins the lobby
        val boardSize = 19
        joinLobbyHttp(token, boardSize)

        // when: the user leaves the lobby
        // then: the response is a 200
        leaveLobbyHttp(token)

        // when: the user tries to leave the lobby again
        // then: the response is a 400
        client.delete().uri(api("/lobby/leave"))
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: the user tries to leave the lobby without a token
        // then: the response is a 401
        client.delete().uri(api("/lobby/leave"))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: the user tries to leave the lobby with a wrong token
        // then: the response is a 401
        client.delete().uri(api("/lobby/leave"))
            .header("Authorization", "Bearer 1234124512")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")
    }

    @Test
    fun `two users join lobby and try to find their match`() {
        // given: two users
        val (name, email, password) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name, email, password)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token = loginTestUserHttp(name, password)
        val token2 = loginTestUserHttp(name2, password2)

        resetLobby()

        // when: the users join the lobby
        val boardSize = 15
        joinLobbyHttp(token, boardSize)
        joinLobbyHttp(token2, boardSize)

        // and: the users try to find their match
        val gameLink = findMatchAndGetGameLink(token)
        val gameLink2 = findMatchAndGetGameLink(token2)

        // then: they find the match and the response is a 200
        assertEquals(gameLink, gameLink2)

        // when: a user tries to find a match without a token
        // then: the response is a 401
        client.get().uri(api("/lobby/matchmaking"))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: a user tries to find a match with a wrong token
        // then: the response is a 401
        client.get().uri(api("/lobby/matchmaking"))
            .header("Authorization", "Bearer 1234124512")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")
    }
}

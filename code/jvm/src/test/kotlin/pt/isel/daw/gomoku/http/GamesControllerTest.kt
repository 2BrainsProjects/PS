package pt.isel.daw.gomoku.http

import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import pt.isel.daw.gomoku.domain.game.Color
import pt.isel.daw.gomoku.domain.game.GameDomain.Companion.RATING_CHANGE
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.http.controllers.game.models.GetGamesCountOutputModel
import pt.isel.daw.gomoku.http.hypermedia.SirenEntityEmbeddedRepresentationModel
import pt.isel.daw.gomoku.http.hypermedia.getActionLink
import pt.isel.daw.gomoku.http.utils.Rels
import pt.isel.daw.gomoku.services.models.PieceModel
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GamesControllerTest : HttpTest() {

    @Test
    fun `two users join a game`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token1 = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)
        resetLobby()

        // and: the users join the lobby
        joinLobbyHttp(token1)
        joinLobbyHttp(token2)

        // then: the users find a match
        val findMatchSiren = findMatchHttp(token1)
        val getGameLink = findMatchSiren.getActionLink(Rels.Game.GAME)!!

        // when: getting the game which the users are in
        val gameSiren = getGameHttp(getGameLink)
        val game = gameSiren.properties!!

        // then: a game is returned
        assertEquals(emptyList(), game.board.pieces)
        assertEquals(GameState.RUNNING.toString(), game.state)
        assertEquals(Color.BLACK.toString(), game.turn)

        // when: getting the users in the game
        val getBlackPlayerLink = gameSiren.getActionLink(Rels.Game.BLACK_PLAYER)!!
        val getWhitePlayerLink = gameSiren.getActionLink(Rels.Game.WHITE_PLAYER)!!
        val blackPlayer = getUserHttp(getBlackPlayerLink).properties!!
        val whitePlayer = getUserHttp(getWhitePlayerLink).properties!!

        // then: the users are returned
        assertEquals(name1, blackPlayer.name)
        assertEquals(name2, whitePlayer.name)
    }

    @Test
    fun `cannot create a game with a single user`() {
        // given: a user
        val (name1, email1, password1) = testUserData()

        // when: registering the user
        registerTestUserHttp(name1, email1, password1)

        // and: logging in the user
        val token1 = loginTestUserHttp(name1, password1)

        resetLobby()

        // when: the user joins an empty lobby
        val gameLink = joinLobbyHttp(token1)

        // then: no game is returned/created
        assertNull(gameLink)
    }

    @Test
    fun `user plays`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token1 = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)
        resetLobby()

        // and: the users join the lobby
        joinLobbyHttp(token1)
        joinLobbyHttp(token2)

        // then: the users find a match
        val findMatchSiren = findMatchHttp(token1)
        val gameLink = findMatchSiren.getActionLink(Rels.Game.GAME)!!

        // and: a game is created
        val game = getGameHttp(gameLink).properties!!
        assertNotNull(game)
        assertEquals(emptyList(), game.board.pieces)

        // when: the first user plays
        // then: the response is 200
        playHttp(token1, 1, 1)

        // when: a user tries to play without a token
        // then: the response is 401
        client.put().uri(api("/game/play"))
            .bodyValue(mapOf("row" to 1, "col" to 1))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: a user tries to play with a wrong token
        // then: the response is 401
        client.put().uri(api("/game/play"))
            .header("Authorization", "Bearer 12321412")
            .bodyValue(mapOf("row" to 1, "col" to 1))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: getting the game after the first user played
        // then: the game has a piece
        val gameUpdated = getGameHttp(gameLink).properties!!
        assertEquals(1, gameUpdated.board.pieces.size)
        assertEquals(PieceModel(1, 1, "B"), gameUpdated.board.pieces.first())
    }

    @Test
    fun `user try to play in wrong turn`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token1 = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)

        resetLobby()

        // and: the users join the lobby
        joinLobbyHttp(token1)
        joinLobbyHttp(token2)

        // then: the users find a match
        val siren = findMatchHttp(token1)

        // and: a game is created
        val getGameLink = siren.entities?.first { it.rel.contains(Rels.Game.GAME) }?.href.toString()
        val game = getGameHttp(getGameLink).properties!!
        assertNotNull(game)
        assertEquals(emptyList(), game.board.pieces)

        // when: the user tries to play in the wrong turn
        // then: the response is 400
        client.put().uri(api("/game/play"))
            .header("Authorization", "Bearer $token2")
            .bodyValue(mapOf("row" to 1, "col" to 1))
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().valueEquals("Content-Type", "application/problem+json")
    }

    @Test
    fun `user get games`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        // then: the users are logged in
        val token = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)

        // when: user1 gets his games
        val initUser1Games = client.get().uri(api("/games?username=$name1"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetGamesCountOutputModel>>()
            .returnResult().responseBody?.properties!!

        // then: the user has no games
        assertNotNull(initUser1Games)
        assertEquals(0, initUser1Games.count)

        // when: both users join the lobby
        // then: the users find a match
        // and: a game is created
        joinLobbyHttp(token)
        joinLobbyHttp(token2)

        // when: user1 gets his games
        val user1Games = client.get().uri(api("/games?username=$name1"))
            .exchange()
            .expectStatus().isOk
            .expectBody<SirenEntityEmbeddedRepresentationModel<GetGamesCountOutputModel>>()
            .returnResult().responseBody?.properties!!

        // then: the user has 1 game
        assertEquals(1, user1Games.count)
    }

    @Test
    fun `user leaves a game`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token1 = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)

        resetLobby()

        // and: the users join the lobby
        joinLobbyHttp(token1)
        joinLobbyHttp(token2)

        // then: the users find a match
        val findMatchSiren = findMatchHttp(token1)
        val gameLink = findMatchSiren.getActionLink(Rels.Game.GAME)!!

        // when: user1 plays
        playHttp(token1, 1, 1)

        // and: user1 leaves
        // then: the response is 200
        client.put().uri(api("/game/leave"))
            .header("Authorization", "Bearer $token1")
            .exchange()
            .expectStatus().isOk

        // when: a user tries to leave a game without a token
        // then: the response is 401
        client.put().uri(api("/game/leave"))
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: a user tries to leave a game with a wrong token
        // then: the response is 401
        client.put().uri(api("/game/play"))
            .header("Authorization", "Bearer 12321412")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().valueEquals("Content-Type", "application/problem+json")

        // when: getting the game after user1 left
        val game = getGameHttp(gameLink).properties!!

        // then: the game finished and user2 won
        assertEquals(GameState.WHITE_WON.toString(), game.state)
    }

    @Test
    fun `when a game finishes, the users ratings are updated`() {
        // given: two users
        val (name1, email1, password1) = testUserData()
        val (name2, email2, password2) = testUserData()

        // when: registering the users
        registerTestUserHttp(name1, email1, password1)
        registerTestUserHttp(name2, email2, password2)

        // and: logging in the users
        val token1 = loginTestUserHttp(name1, password1)
        val token2 = loginTestUserHttp(name2, password2)

        resetLobby()

        // and: the users join the lobby
        joinLobbyHttp(token1)
        joinLobbyHttp(token2)

        // then: the users find a match
        val findMatchSiren = findMatchHttp(token1)
        val gameLink = findMatchSiren.getActionLink(Rels.Game.GAME)!!

        // when: user1 plays
        playHttp(token1, 1, 1)

        // and: user1 leaves
        // then: the response is 200
        client.put().uri(api("/game/leave"))
            .header("Authorization", "Bearer $token1")
            .exchange()
            .expectStatus().isOk

        // when: getting the game after user1 left
        // and: getting the users that played the game
        val gameSiren = getGameHttp(gameLink)
        val getBlackPlayerLink = gameSiren.getActionLink(Rels.Game.BLACK_PLAYER)!!
        val getWhitePlayerLink = gameSiren.getActionLink(Rels.Game.WHITE_PLAYER)!!
        val blackPlayer = getUserHttp(getBlackPlayerLink).properties!!
        val whitePlayer = getUserHttp(getWhitePlayerLink).properties!!

        // then: the game finished
        // and: user2 won
        // and: the users ratings are updated
        assertEquals(GameState.WHITE_WON.toString(), gameSiren.properties?.state)
        assertEquals(initialRating + RATING_CHANGE, whitePlayer.stats.rating)
        assertEquals(initialRating - RATING_CHANGE, blackPlayer.stats.rating)
    }
}

package pt.isel.daw.gomoku.services

import pt.isel.daw.gomoku.domain.exceptions.GameException.GameNotFoundException
import pt.isel.daw.gomoku.domain.exceptions.GameException.UserNotInGameException
import pt.isel.daw.gomoku.domain.exceptions.UserException.UserNotFoundException
import pt.isel.daw.gomoku.domain.game.GameDomain.Companion.RATING_CHANGE
import pt.isel.daw.gomoku.domain.game.GameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GameServiceTest : ServicesTest() {

    @Test
    fun `try to get an non existent game`() {
        // when: trying to get a non-existent game
        // then: an exception is thrown
        assertFailsWith<GameNotFoundException> { gameService.getGame(9999) }
    }

    @Test
    fun `makes a move`() {
        resetLobby()
        // given: a game with two players
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig) // game created
        val gameId = lobbyService.findMatch(user1.id)?.gameId
        assertNotNull(gameId)

        // when: both players make a move
        gameService.playGame(user1.id, 5, 10)
        gameService.playGame(user2.id, 10, 10)

        // then: the game has two pieces
        val game = gameService.getGame(gameId)
        assertEquals(game.board.pieces.size, 2)
    }

    @Test
    fun `trying to make a move with an non existent user`() {
        // when: trying to make a move with a non-existent user
        // then: an exception is thrown
        assertFailsWith<UserNotFoundException> { gameService.playGame(Int.MAX_VALUE, 5, 10) }
    }

    @Test
    fun `trying to make a move with an user not in a game`() {
        // given: a user
        val user = registerAndGetTestUser()

        // when: trying to make a move with a user not in a game
        // then: an exception is thrown
        assertFailsWith<UserNotInGameException> { gameService.playGame(user.id, 5, 10) }
    }

    @Test
    fun `get games from a user`() {
        // given: two users
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()
        val (games1) = gameService.getGames(user1.name, null, 0, 5, "asc")
        assertEquals(0, games1.size)

        // when: a game is created with both users in it
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig) // game created

        // then: when one of the users gets his games, the game is there
        val (games2) = gameService.getGames(user1.name, null, 0, 5, "asc")
        assertEquals(1, games2.size)
    }

    @Test
    fun `finish the first game and getGames on the second`() {
        resetLobby()
        // given: two users
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()

        // when: a game is created with both users in it
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig) // game created
        val gameId = lobbyService.findMatch(user1.id)?.gameId

        // then: both users have one game
        assertNotNull(gameId)
        val game = gameService.getGame(gameId)
        val (games) = gameService.getGames(user1.name, null, 0, 5, "asc")
        assertEquals(1, games.size)
        assertEquals(user1.id, game.blackPlayerId)
        assertEquals(user2.id, game.whitePlayerId)

        // when: both players make a move in order for the game to end (black wins)
        (1..4).forEach {
            gameService.playGame(user1.id, 1, it)
            gameService.playGame(user2.id, 2, it)
        }
        gameService.playGame(user1.id, 1, 5)

        // then: the game is finished and the black player won
        assertEquals(GameState.BLACK_WON, gameService.getGame(gameId).state)

        // and: the black player has his rating increased
        val user1Stats = usersServices.getUser(user1.id).stats
        assertEquals(600 + RATING_CHANGE, user1Stats.rating)

        // when: a new game is created with both users in it
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig) // game created

        // then: both users have two games
        assertEquals(2, gameService.getGames(user1.name, null, 0, 5, "asc").games.size)
        assertEquals(2, gameService.getGames(user2.name, null, 0, 2, "asc").games.size)
        assertEquals(1, gameService.getGames(user2.name, null, 1, 2, "asc").games.size)
    }

    @Test
    fun `user leaves an ongoing game`() {
        resetLobby()

        // given: two users
        val user1 = registerAndGetTestUser()
        val user2 = registerAndGetTestUser()

        // when: a game is created with both users in it
        lobbyService.joinLobby(user1, testGameConfig)
        lobbyService.joinLobby(user2, testGameConfig) // game created
        val gameId = lobbyService.findMatch(user1.id)?.gameId
        assertNotNull(gameId)

        // and: both players make a move
        gameService.playGame(user1.id, 1, 1)
        gameService.playGame(user2.id, 2, 2)

        // and: the black player leaves the game
        gameService.leaveGame(user1.id)

        // then: the game is finished and the white player won
        val game = gameService.getGame(gameId)
        assertEquals(GameState.WHITE_WON, game.state)
    }
}

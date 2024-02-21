package pt.isel.daw.gomoku.repository

import pt.isel.daw.gomoku.domain.game.Board
import pt.isel.daw.gomoku.domain.game.Color
import pt.isel.daw.gomoku.domain.game.Coordinate
import pt.isel.daw.gomoku.domain.game.Game
import pt.isel.daw.gomoku.domain.game.GameState
import pt.isel.daw.gomoku.domain.game.Piece
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GameRepositoryTest : RepositoryTest() {

    @Test
    fun `create and retrieve a game`() {
        // given: users
        val user1 = registerTestUser()
        val user2 = registerTestUser()

        // when: creating a game
        val board = Board(testBoardSize)
        val gameConfig = testGameConfig
        val gameId = gameRepository.createGame(board, user1, user2, gameConfig)

        // then: the game is created
        val game = gameRepository.getGame(gameId)
        assertEquals(gameId, game.id)
        assertEquals(board, game.board)
        assertEquals(gameConfig, game.config)

        // when: checking if the game exists
        val isGameIsStored = gameRepository.isGame(gameId)

        // then: response is true
        assertTrue(isGameIsStored)

        // when: checking if a non-existent game exists
        val nonExistentGameIsStored = gameRepository.isGame(9999)

        // then: response is false
        assertFalse(nonExistentGameIsStored)
    }

    @Test
    fun `checking if that id is a valid game`() {
        // when: creating a game and getting its id
        val gameId = createAndGetTestGame().id

        // then: the id represents a valid game
        assertTrue(gameRepository.isGame(gameId))
        assertFalse(gameRepository.isGame(9999))
    }

    @Test
    fun `check if the game is running when created`() {
        // when: creating a game
        val game = createAndGetTestGame()

        // then: the game is running
        assertFalse(game.isOver)
        assertEquals(game.state, GameState.RUNNING)
    }

    @Test
    fun `update a game`() {
        // when: creating a game
        val board = Board(testBoardSize)
        val gameConfig = testGameConfig
        val gameId = createAndGetTestGame(board).id

        // and: updating the game
        val updatedBoard = Board(board.size, listOf(Piece(Coordinate(1, 1), Color.BLACK)))
        val updatedGame = Game(gameId, updatedBoard, 1, 2, gameConfig, Color.WHITE)
        gameRepository.updateGame(updatedGame)

        // then: the game is updated
        val game = gameRepository.getGame(gameId)
        assertEquals(updatedGame.id, game.id)
        assertEquals(updatedGame.turn, Color.WHITE)
        assertEquals(updatedGame.board, game.board)
    }

    @Test
    fun `check if game ends after 5 pieces in a row`() {
        // when: creating a game
        val game = createAndGetTestGame()

        // then: the game is running
        assertFalse(game.isOver)
        assertEquals(GameState.RUNNING, game.state)

        // when: updating the game with 5 pieces in a row so that the black player wins
        val updatedBoard = Board(
            game.board.size,
            listOf(
                Piece(Coordinate(1, 1), Color.BLACK),
                Piece(Coordinate(2, 1), Color.WHITE),
                Piece(Coordinate(1, 2), Color.BLACK),
                Piece(Coordinate(2, 2), Color.WHITE),
                Piece(Coordinate(1, 3), Color.BLACK),
                Piece(Coordinate(2, 3), Color.WHITE),
                Piece(Coordinate(1, 4), Color.BLACK),
                Piece(Coordinate(2, 4), Color.WHITE),
                Piece(Coordinate(1, 5), Color.BLACK)
            )
        )
        val updatedGame = game.copy(
            board = updatedBoard,
            turn = Color.BLACK,
            state = GameState.BLACK_WON
        )
        gameRepository.updateGame(updatedGame)

        // then: the game is over and the black player won
        val gameAfterUpdate = gameRepository.getGame(game.id)
        assertEquals(GameState.BLACK_WON, gameAfterUpdate.state)
        assertTrue(gameRepository.isGameOver(gameAfterUpdate.id))
    }

    @Test
    fun `check if user is in game and get the match the user is in`() {
        // when: registering a user
        val userId = registerTestUser()

        // then: the user is not in a game
        assertFalse(gameRepository.isUserInGame(userId))

        // when: creating a game with the user
        val gameId = gameRepository.createGame(Board(testBoardSize), userId, registerTestUser(), testGameConfig)

        // then: the user is in a game
        assertEquals(gameId, gameRepository.getMatch(userId).gameId)
    }

    @Test
    fun `get Games from a user`() {
        // given: users
        val user1 = registerTestUser()
        val user2 = registerTestUser()
        val user3 = registerTestUser()

        // when: creating games with the users
        val game1 = createAndGetTestGame(user1 = user1, user2 = user2)
        val game2 = createAndGetTestGame(user1 = user1, user2 = user3)
        createAndGetTestGame(user1 = user2, user2 = user3)

        // then: the games contain the respective users
        val gamesUser1 = gameRepository.getGames(user1, null, 0, 10, "asc")
        assertEquals(2, gamesUser1.size)
        assertContains(gamesUser1, game1)
        assertContains(gamesUser1, game2)

        val gamesUser2 = gameRepository.getGames(user2, null, 1, 10, "asc")
        assertEquals(1, gamesUser2.size)
    }
}

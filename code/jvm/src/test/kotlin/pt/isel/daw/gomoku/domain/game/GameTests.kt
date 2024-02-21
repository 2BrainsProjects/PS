package pt.isel.daw.gomoku.domain.game

import pt.isel.daw.gomoku.domain.game.BoardTests.Companion.BOARD_SIZE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameTests {

    @Test
    fun `create a new game`() {
        // when: creating a game
        val game = createTestGame()

        // then: the board is empty, the turn is black and the game is running
        assertTrue(game.board.pieces.isEmpty())
        assertEquals(game.turn, Color.BLACK)
        assertEquals(GameState.RUNNING, game.state)
    }

    @Test
    fun `switch turns when making a move`() {
        // given: a game
        val game = createTestGame()

        // when: making a move
        val newGame = domain.play(game, 1, 1, Color.BLACK)

        // then: the turn is changed
        assertEquals(Color.WHITE, newGame.turn)
    }

    @Test
    fun `check if the game is a draw when the board is full and there are no winners`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces in every position
        repeat(BOARD_SIZE) { row ->
            repeat(BOARD_SIZE) { col ->
                val isBlack = game.turn == Color.BLACK
                val isColumnEven = col % 2 == 0
                val isRowEven = row % 4 == 0 || (row - 1) % 4 == 0

                if (isBlack == isColumnEven == isRowEven) {
                    game = domain.play(game, row + 1, col + 1, game.turn)
                }
            }
        }
        for (i in 1..BOARD_SIZE / 2) {
            game = domain.play(game, 2 * i, 1, game.turn)
        }

        // then: the board is full and the game is a draw
        assertTrue(game.board.isFull)
        assertTrue(game.isOver)
        assertEquals(GameState.DRAW, game.state)
    }

    @Test
    fun `check if black won the game vertically`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces vertically in a way that black wins
        game = playBothVertically(game, 1, 2, 4)
        game = domain.play(game, 5, 1, Color.BLACK)

        // then: the game is over and black won
        assertTrue(game.isOver)
        assertEquals(GameState.BLACK_WON, game.state)
    }

    @Test
    fun `check if black won the game horizontally`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces horizontally in a way that black wins
        game = playBothHorizontally(game, 1, 2, 4)
        game = domain.play(game, 1, 5, Color.BLACK)

        // then: the game is over and black won
        assertTrue(game.isOver)
        assertEquals(GameState.BLACK_WON, game.state)
    }

    @Test
    fun `check if black won the game diagonally`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces diagonally in a way that black wins
        game = playBothDiagonally(game, 4)
        game = domain.play(game, 5, 5, Color.BLACK)

        // then: the game is over and black won
        assertTrue(game.isOver)
        assertEquals(GameState.BLACK_WON, game.state)
    }

    @Test
    fun `check if white won the game vertically`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces vertically in a way that white wins
        game = playBothVertically(game, 1, 2, 4)
        game = domain.play(game, 10, 1, Color.BLACK)
        game = domain.play(game, 5, 2, Color.WHITE)

        // then: the game is over and white won
        assertTrue(game.isOver)
        assertEquals(GameState.WHITE_WON, game.state)
    }

    @Test
    fun `check if white won the game horizontally`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces horizontally in a way that white wins
        game = playBothHorizontally(game, 1, 2, 4)
        game = domain.play(game, 3, 5, Color.BLACK)
        game = domain.play(game, 2, 5, Color.WHITE)

        // then: the game is over and white won
        assertTrue(game.isOver)
        assertEquals(GameState.WHITE_WON, game.state)
    }

    @Test
    fun `check if white won the game diagonally`() {
        // given: a game
        var game = createTestGame()

        // when: playing pieces diagonally in a way that white wins
        game = playBothDiagonally(game, 4)
        game = domain.play(game, 1, 5, Color.BLACK)
        game = domain.play(game, 6, 5, Color.WHITE)

        // then: the game is over and white won
        assertTrue(game.isOver)
        assertEquals(GameState.WHITE_WON, game.state)
    }

    companion object {
        val domain = GameDomain()

        private fun createTestGame(board: Board = Board(BOARD_SIZE)) =
            Game(
                id = 1,
                board = board,
                blackPlayerId = 1,
                whitePlayerId = 2,
                config = GameConfig(BOARD_SIZE, Variant.FREESTYLE, Opening.FREESTYLE)
            )

        fun GameDomain.play(game: Game, row: Int, col: Int, turn: Color) =
            makeMove(game, Move(Coordinate(row, col), turn))

        fun playBothDiagonally(game: Game, n: Int): Game =
            (1..n).fold(game) { g, i ->
                domain.play(domain.play(g, i, i, Color.BLACK), i + 1, i, Color.WHITE)
            }

        fun playBothVertically(game: Game, bCol: Int, wCol: Int, n: Int): Game =
            (1..n).fold(game) { g, i ->
                domain.play(domain.play(g, i, bCol, Color.BLACK), i, wCol, Color.WHITE)
            }

        fun playBothHorizontally(game: Game, bRow: Int, wRow: Int, n: Int): Game =
            (1..n).fold(game) { g, i ->
                domain.play(domain.play(g, bRow, i, Color.BLACK), wRow, i, Color.WHITE)
            }
    }
}

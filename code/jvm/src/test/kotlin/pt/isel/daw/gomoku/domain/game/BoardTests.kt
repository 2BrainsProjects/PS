package pt.isel.daw.gomoku.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BoardTests {

    @Test
    fun `board should be empty when created`() {
        // when: creating a board
        val board = Board(BOARD_SIZE)

        // then: the board is empty
        assertTrue(board.pieces.isEmpty())
        assertFalse(board.isFull)
    }

    @Test
    fun `add a piece to the board`() {
        // given: a board and a piece
        val board = Board(BOARD_SIZE)
        val piece = Piece(Coordinate(1, 1), Turn.BLACK)

        // when: adding the piece to the board
        val newBoard = board.addPiece(piece.coord, piece.color)

        // then: the board has the piece
        assertEquals(listOf(piece), newBoard.pieces)
    }

    @Test
    fun `try to add a piece to the board in a position that is already occupied`() {
        // given: a board
        var board = Board(BOARD_SIZE)

        // when: adding a piece to the board
        board = board.addPiece(Coordinate(1, 2), Turn.BLACK)

        // then: the trying to add a piece to the same position throws an exception
        assertFailsWith<IllegalArgumentException> {
            board = board.addPiece(Coordinate(1, 2), Turn.WHITE)
        }
    }

    @Test
    fun `try to add a piece to the board in a position that is outside the board`() {
        // given: a board
        val board = Board(BOARD_SIZE)

        // then: trying to add a piece to a position outside the board throws an exception
        assertFailsWith<IllegalArgumentException> {
            board.addPiece(Coordinate(1, BOARD_SIZE + 1), Turn.BLACK)
        }
    }

    companion object {
        const val BOARD_SIZE = 15
    }
}

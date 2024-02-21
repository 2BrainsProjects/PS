import { Piece } from './Piece';

export class BoardModel {
  private readonly pieces: Piece[] = [];

  constructor(
    public size: number,
    pieces: Piece[] = []
  ) {
    if (![15, 19].includes(size)) {
      throw new Error('Board size must be 15 or 19');
    }

    if (
      !pieces.every(
        piece => piece.coord.row >= 1 && piece.coord.row <= size && piece.coord.col >= 1 && piece.coord.col <= size
      )
    ) {
      throw new Error('All pieces must be inside the board');
    }

    if (pieces.length !== new Set(pieces.map(piece => piece.coord.toString())).size) {
      throw new Error('All pieces should have different coordinates');
    }

    this.pieces = pieces;
  }

  getPieceAt(row: number, col: number): Piece | undefined {
    return this.pieces.find(piece => piece.coord.row === row && piece.coord.col === col);
  }
}

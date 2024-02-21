import { Color } from '../../../../domain/game/Color';
import { Coordinate } from '../../../../domain/game/Coordinate';
import { Piece } from '../../../../domain/game/Piece';

export class PieceOutputModel {
  constructor(
    public row: number,
    public col: number,
    public color: Color
  ) {}

  public toPiece(): Piece {
    return new Piece(new Coordinate(this.row, this.col), this.color);
  }
}

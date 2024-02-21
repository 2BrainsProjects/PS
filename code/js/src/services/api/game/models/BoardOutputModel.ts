import { PieceOutputModel } from './PieceOutputModel';
import { BoardModel } from '../../../../domain/game/BoardModel';

export class BoardOutputModel {
  constructor(
    public size: number,
    public pieces: PieceOutputModel[]
  ) {}

  public toBoard(): BoardModel {
    return new BoardModel(
      this.size,
      this.pieces.map(p => new PieceOutputModel(p.row, p.col, p.color).toPiece())
    );
  }
}

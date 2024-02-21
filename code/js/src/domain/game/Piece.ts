import { Coordinate } from './Coordinate';
import { Color } from './Color';

export class Piece {
  constructor(
    public coord: Coordinate,
    public color: Color
  ) {}

  static getColor(piece: Piece | undefined): string {
    switch (piece?.color) {
      case Color.BLACK:
        return '#2c90ff';
      case Color.WHITE:
        return 'white';
      default:
        return 'transparent';
    }
  }
}

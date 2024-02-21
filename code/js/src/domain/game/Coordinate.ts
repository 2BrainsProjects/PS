export class Coordinate {
  constructor(
    public row: number,
    public col: number
  ) {}

  toString(): string {
    return `${this.row}:${this.col}`;
  }
}

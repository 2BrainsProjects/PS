export class GetGameOutputModel {
  constructor(
    public gameState: string,
    public opponentColor?: string
  ) {}
}

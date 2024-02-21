export class Stats {
  constructor(
    public rating: number,
    public gamesPlayed: number = 0,
    public wins: number = 0,
    public draws: number = 0,
    public losses: number = 0
  ) {}
}

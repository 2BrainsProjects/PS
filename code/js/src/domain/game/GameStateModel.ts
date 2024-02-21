import { BoardModel } from './BoardModel';
import { Color } from './Color';
import { GameState } from './GameState';
import { PlayerModel } from './Player';

export class GameStateModel {
  constructor(
    public board: BoardModel,
    public turn: Color,
    public state: GameState
  ) {}
}

export function getStateMessage(game: GameStateModel, me: PlayerModel, opponent: PlayerModel): string {
  const black = me.color === Color.BLACK ? me : opponent;
  const white = me.color === Color.WHITE ? me : opponent;
  switch (game.state) {
    case GameState.RUNNING:
      return `${game.turn === Color.WHITE ? 'White' : 'Blue'} to move`;
    case GameState.BLACK_WON:
      return `${black.user.name} won!`;
    case GameState.WHITE_WON:
      return `${white.user.name} won!`;
    case GameState.DRAW:
      return 'Draw!';
  }
}

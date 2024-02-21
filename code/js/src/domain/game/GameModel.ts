import { User } from '../user/User';
import { Color } from './Color';
import { GameState } from './GameState';

export class GameModel {
  constructor(
    public state: GameState,
    public black: User,
    public white: User,
    public link: string,
    public opponent: Color | null
  ) {}
}

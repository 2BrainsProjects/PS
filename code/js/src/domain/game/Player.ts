import { User } from '../user/User';
import { Color } from './Color';

export class PlayerModel {
  constructor(
    public user: User,
    public color: Color
  ) {}
}

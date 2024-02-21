import { Stats } from '../../../../domain/user/Stats';

export class GetUserOutputModel {
  constructor(
    public name: string,
    public email: string,
    public stats: Stats
  ) {}
}

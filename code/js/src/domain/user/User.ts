import { Stats } from './Stats';

export class User {
  constructor(
    public name: string,
    public email: string,
    public stats: Stats,
    public link?: string
  ) {}
}

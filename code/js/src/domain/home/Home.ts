import { AuthorModel } from './Author';

export class HomeModel {
  constructor(
    public title: string,
    public version: string,
    public description: string,
    public authors: AuthorModel[],
    public repository: string
  ) {}
}

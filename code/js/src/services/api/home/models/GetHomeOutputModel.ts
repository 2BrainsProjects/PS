import { AuthorOutputModel } from './AuthorOutputModel';

export class GetHomeOutputModel {
  constructor(
    public title: string,
    public version: string,
    public description: string,
    public authors: AuthorOutputModel[],
    public repository: string
  ) {}
}

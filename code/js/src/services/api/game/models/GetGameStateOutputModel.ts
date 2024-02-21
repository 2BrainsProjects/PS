import { BoardOutputModel } from './BoardOutputModel';

export class GetGameStateOutputModel {
  constructor(
    public board: BoardOutputModel,
    public turn: string,
    public state: string
  ) {}
}

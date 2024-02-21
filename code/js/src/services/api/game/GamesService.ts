import { HttpService } from '../../HttpService';
import { PlayGameInputModel } from './models/PlayGameInputModel';
import { GetGameStateOutputModel } from './models/GetGameStateOutputModel';
import { GameStateModel } from '../../../domain/game/GameStateModel';
import { GetGamesOutputModel } from './models/GetGamesOutputModel';
import { GameModel } from '../../../domain/game/GameModel';
import { User } from '../../../domain/user/User';
import { Color } from '../../../domain/game/Color';
import { GetGameOutputModel } from './models/GetGameOutputModel';
import { SirenEntity } from '../../media/siren/SirenEntity';
import { GetUserOutputModel } from '../user/models/GetUserOutputModel';
import Rels from '../../Rels';
import '../../../domain/game/GameState';
import { GameState } from '../../../domain/game/GameState';
import { getActionLink, getEmbeddedRepresentations } from '../../media/siren/Utils';
import { BoardOutputModel } from './models/BoardOutputModel';
import { Result, runCatching } from '../../Result';

export class GamesService {
  constructor(public http: HttpService) {}

  async play(link: string, row: number, column: number): Promise<Result<void>> {
    const result = await runCatching(this.http.put<void>(link, true, new PlayGameInputModel(row, column)));
    return result.mapVoid();
  }

  async getGame(link: string): Promise<Result<GameStateModel>> {
    const result = await runCatching(this.http.get<GetGameStateOutputModel>(link));
    return result.map(siren => {
      const game = siren.properties;
      if (game === undefined) throw new Error('Game is undefined');
      return new GameStateModel(
        new BoardOutputModel(game.board.size, game.board.pieces).toBoard(),
        game.turn.toColor(),
        game.state.toGameState()
      );
    });
  }

  async leaveGame(link: string): Promise<Result<void>> {
    const result = await runCatching(this.http.put<void>(link, true));
    return result.mapVoid();
  }

  async getGames(
    link: string,
    username?: string,
    state?: GameState,
    page?: number,
    limit?: number,
    sort?: string
  ): Promise<Result<GameModel[]>> {
    const params = { username, state, page, limit, sort };
    const result = await runCatching(this.http.get<GetGamesOutputModel>(link, false, params));
    return result.map(siren => {
      if (siren.properties === undefined) throw new Error('Games count is undefined');
      const representations = getEmbeddedRepresentations<GetGamesOutputModel, GetGameOutputModel>(
        siren,
        Rels.GAME,
        Rels.ITEM
      );
      return representations.map(rep => {
        const game = rep.properties;
        if (game === undefined) throw new Error('Game is undefined');
        return new GameModel(
          game.gameState.toGameState(),
          this.getGameUser(rep, Color.BLACK),
          this.getGameUser(rep, Color.WHITE),
          getActionLink(rep, Rels.GAME),
          game.opponentColor?.toColor() ?? null
        );
      });
    });
  }

  private getGameUser(siren: SirenEntity<GetGameOutputModel>, color: Color): User {
    const rel = color === Color.BLACK ? Rels.BLACK_PLAYER : Rels.WHITE_PLAYER;
    const representation = getEmbeddedRepresentations<GetGameOutputModel, GetUserOutputModel>(siren, rel);
    const user = representation[0].properties;
    if (user === undefined) throw new Error('User is undefined');
    return new User(user.name, user.email, user.stats);
  }
}

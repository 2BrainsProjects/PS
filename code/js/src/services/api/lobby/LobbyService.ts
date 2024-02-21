import { HttpService } from '../../HttpService';
import { FindMatchOutputModel } from './models/FindMatchOutputModel';
import { Result, runCatching } from '../../Result';
import { Color } from '../../../domain/game/Color';

export class LobbyService {
  constructor(public http: HttpService) {}

  async joinLobby(link: string, boardSize: number, variant: string, opening: string): Promise<Result<void>> {
    const result = await runCatching(this.http.post<void>(link, true, { boardSize, variant, opening }));
    return result.mapVoid();
  }

  async findMatch(link: string): Promise<Result<Color | undefined>> {
    const result = await runCatching(this.http.get<FindMatchOutputModel>(link, true));
    return result.map(siren => {
      const color = siren.properties?.color;
      return color?.toColor();
    });
  }

  async leaveLobby(link: string): Promise<Result<void>> {
    const result = await runCatching(this.http.delete<void>(link, true));
    return result.mapVoid();
  }
}

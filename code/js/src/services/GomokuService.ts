import { HttpService } from './HttpService';
import { UsersService } from './api/user/UsersService';
import { HomeService } from './api/home/HomeService';
import { LobbyService } from './api/lobby/LobbyService';
import { GamesService } from './api/game/GamesService';

export class AnonichatService {
  readonly homeService: HomeService;
  readonly usersService: UsersService;
  readonly lobbyService: LobbyService;
  readonly gamesService: GamesService;

  constructor(http: HttpService) {
    this.homeService = new HomeService(http);
    this.usersService = new UsersService(http);
    this.lobbyService = new LobbyService(http);
    this.gamesService = new GamesService(http);
  }
}

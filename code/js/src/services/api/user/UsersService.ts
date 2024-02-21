import { User } from '../../../domain/user/User';
import { GetUserOutputModel } from './models/GetUserOutputModel';
import { GetUsersOutputModel } from './models/GetUsersOutputModel';
import { LoginOutputModel } from './models/LoginOutputModel';
import { getActionLink, getEmbeddedRepresentations } from '../../media/siren/Utils';
import { HttpService } from '../../HttpService';
import Rels from '../../Rels';
import { Result, runCatching } from '../../Result';

export class UsersService {
  constructor(public http: HttpService) {}

  async getUserHome(link: string): Promise<Result<GetUserOutputModel>> {
    const result = await runCatching(this.http.get<GetUserOutputModel>(link, true));
    return result.map(siren => siren.properties as GetUserOutputModel);
  }

  async register(link: string, name: string, email: string, password: string): Promise<Result<void>> {
    const result = await runCatching(this.http.post<void>(link, false, { name, email, password }));
    return result.mapVoid();
  }

  async login(
    link: string,
    name: string | undefined,
    email: string | undefined,
    password: string
  ): Promise<Result<void>> {
    const result = await runCatching(this.http.post<LoginOutputModel>(link, true, { name, email, password }));
    return result.mapVoid();
  }

  async logout(link: string): Promise<Result<void>> {
    const result = await runCatching(this.http.post<void>(link, true));
    return result.mapVoid();
  }

  async getUser(link: string): Promise<Result<User>> {
    const result = await runCatching(this.http.get<GetUserOutputModel>(link));
    return result.map(siren => {
      const user = siren.properties;
      if (user === undefined) throw new Error('User is undefined');
      return new User(user.name, user.email, user.stats, link);
    });
  }

  async getUsers(
    link: string,
    page?: number,
    limit?: number,
    orderBy?: string,
    sort?: string
  ): Promise<Result<User[]>> {
    const params = { page, limit, orderBy, sort };
    const result = await runCatching(this.http.get<GetUsersOutputModel>(link, false, params));
    return result.map(siren => {
      const representations = getEmbeddedRepresentations<GetUsersOutputModel, GetUserOutputModel>(
        siren,
        Rels.USER,
        Rels.ITEM
      );
      return representations.map(rep => {
        const user = rep.properties;
        if (user === undefined) throw new Error('User is undefined');
        return new User(user.name, user.email, user.stats, getActionLink(rep, Rels.USER));
      });
    });
  }
}

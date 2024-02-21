import { HttpService } from '../../HttpService';
import { GetHomeOutputModel } from './models/GetHomeOutputModel';
import { HomeModel } from '../../../domain/home/Home';
import { AuthorModel } from '../../../domain/home/Author';
import { Result, runCatching } from '../../Result';

const HOME_URI = '/';

export class HomeService {
  constructor(public http: HttpService) {}

  async getHome(): Promise<Result<HomeModel>> {
    const result = await runCatching(this.http.get<GetHomeOutputModel>(HOME_URI));
    return result.map(siren => {
      const { title, version, description, authors, repository } = siren.properties as GetHomeOutputModel;
      return new HomeModel(
        title,
        version,
        description,
        authors.map(author => new AuthorModel(author.name, author.email, author.github, author.number)),
        repository
      );
    });
  }
}

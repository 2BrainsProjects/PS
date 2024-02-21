import { SirenEntity } from './media/siren/SirenEntity';
import { addQuery } from './Utils';
import { getActionLinks, getLinks } from './media/siren/Utils';
import { Problem } from './media/problem/Problem';
import { LinksType } from './LinksService';

const jsonMediaType = 'application/json';
const sirenMediaType = 'application/vnd.siren+json';
const problemMediaType = 'application/problem+json';

export class HttpService {
  constructor(public readonly updateLinks: (links: LinksType, actionLinks: LinksType) => void) {}

  async fetch(uri: string, options: RequestInit | undefined): Promise<any> {
    const response = await fetch(uri, options);
    const json = await response.json();
    const responseContentType = response.headers.get('Content-Type');
    if (!response.ok) {
      if (responseContentType !== problemMediaType) {
        throw new Error('Invalid server response');
      }
      throw new Problem(json.type, json.title, json.detail, json.instance);
    }
    if (responseContentType !== sirenMediaType) {
      throw new Error('Invalid server response');
    }
    return json;
  }

  private async request<T>(uri: string, method: string, body?: object, authenticated?: boolean) {
    const headers = new Headers({
      'Content-Type': jsonMediaType,
      Accept: `${sirenMediaType}, ${problemMediaType}`,
    });

    const res = await this.fetch('/api' + uri, {
      method,
      headers,
      ...(body !== undefined && { body: JSON.stringify(body) }),
      ...(authenticated && { credentials: 'include' }),
    });
    const siren = new SirenEntity<T>(res.class, res.properties, res.links, res.actions, res.entities);
    const links = getLinks(siren);
    const actionLinks = getActionLinks(siren);
    this.updateLinks(links, actionLinks);
    return siren;
  }

  async get<T>(uri: string, authenticated = false, params?: object) {
    return this.request<T>(addQuery(uri, params), 'GET', undefined, authenticated);
  }

  async post<T>(uri: string, authenticated = false, body?: object, params?: object) {
    return this.request<T>(addQuery(uri, params), 'POST', body, authenticated);
  }

  async put<T>(uri: string, authenticated = false, body?: object, params?: object) {
    return this.request<T>(addQuery(uri, params), 'PUT', body, authenticated);
  }

  async delete<T>(uri: string, authenticated = false, body?: object, params?: object) {
    return this.request<T>(addQuery(uri, params), 'DELETE', body, authenticated);
  }
}

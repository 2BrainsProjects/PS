import { Entity } from './Entity';
import { EmbeddedLink, EmbeddedRepresentation } from './SubEntity';

export function getLinks(entity: Entity<unknown>): Record<string, string> {
  return (
    entity.links?.reduce((acc: Record<string, string>, link) => {
      acc[link.rel[0]] = link.href.toString();
      return acc;
    }, {}) || {}
  );
}

export function getActionLinks(entity: Entity<unknown>): Record<string, string> {
  return (
    entity.actions?.reduce((acc: Record<string, string>, action) => {
      acc[action.name] = action.href.toString();
      return acc;
    }, {}) || {}
  );
}

export function getLink(entity: Entity<unknown>, rel: string): string {
  const link = getLinks(entity)[rel];
  if (!link) {
    throw new Error(`No link with rel '${rel}'`);
  }
  return link;
}

export function getActionLink(entity: Entity<unknown>, name: string): string {
  const link = getActionLinks(entity)[name];
  if (!link) {
    throw new Error(`No action link with name '${name}'`);
  }
  return link;
}

export function getEmbeddedLinks<T>(entity: Entity<T>, ...rels: string[]): EmbeddedLink[] {
  const links = entity.entities
    ?.filter(entity => Object.prototype.hasOwnProperty.call(entity, 'href'))
    ?.map(entity => entity as EmbeddedLink)
    ?.filter(entity => rels.every(rel => entity.rel.includes(rel)));

  if (!links) {
    throw new Error('Entity does not have any embedded links');
  }
  return links;
}

export function getEmbeddedRepresentations<T, R>(entity: Entity<T>, ...rels: string[]): EmbeddedRepresentation<R>[] {
  const representations = entity.entities
    ?.filter(entity => !Object.prototype.hasOwnProperty.call(entity, 'href'))
    ?.map(entity => entity as EmbeddedRepresentation<R>)
    ?.filter(entity => rels.every(rel => entity.rel.includes(rel)));

  if (!representations) {
    throw new Error('Entity does not have any embedded representations');
  }
  return representations;
}

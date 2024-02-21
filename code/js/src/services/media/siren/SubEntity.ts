import { Link } from './Link';
import { Action } from './Action';
import { Entity } from './Entity';

export abstract class SubEntity {}

/**
 * Represents a link that represents a navigational transition
 * @param rel The relation of the link
 * @param href The URI of the link
 */
export class EmbeddedLink extends SubEntity {
  constructor(
    public rel: string[],
    public href: string
  ) {
    super();
  }
}

/**
 * Represents a sub-entity that is embedded in an entity
 * @param rel The relation of the sub-entity
 * @param classes The classes of the sub-entity (optional)
 * @param properties The properties of the sub-entity (optional)
 * @param links The links of the sub-entity (optional)
 * @param actions The actions of the sub-entity (optional)
 * @param entities The sub-entities of the sub-entity (optional)
 */
export class EmbeddedRepresentation<T> extends SubEntity implements Entity<T> {
  constructor(
    public rel: string[],
    public classes?: string[],
    public properties?: T,
    public links?: Link[],
    public actions?: Action[],
    public entities?: SubEntity[]
  ) {
    super();
  }
}

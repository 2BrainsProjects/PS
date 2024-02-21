import { Link } from './Link';
import { Action } from './Action';
import { SubEntity } from './SubEntity';
import { Entity } from './Entity';

export class SirenEntity<T> implements Entity<T> {
  constructor(
    public clazz?: string[],
    public properties?: T,
    public links?: Link[],
    public actions?: Action[],
    public entities?: SubEntity[]
  ) {}
}

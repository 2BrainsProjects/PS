import { Action } from './Action';
import { Link } from './Link';
import { SubEntity } from './SubEntity';

export interface Entity<T> {
  class?: string[];
  properties?: T;
  actions?: Action[];
  links?: Link[];
  entities?: SubEntity[];
  title?: string;
}

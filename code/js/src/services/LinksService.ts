export type LinksType = Record<string, string>;

export class LinksService {
  private links: LinksType = {};
  private actionLinks: LinksType = {};

  updateLinks(newLinks: LinksType, newActionLinks: LinksType) {
    this.links = { ...this.links, ...newLinks };
    this.actionLinks = { ...this.actionLinks, ...newActionLinks };
  }

  getLink(rel: string): string {
    const link = this.links[rel];
    if (link === undefined) throw new Error(`No link with rel '${rel}'`);
    return link;
  }

  getActionLink(rel: string): string {
    const link = this.actionLinks[rel];
    if (link === undefined) throw new Error(`No action link with rel '${rel}'`);
    return link;
  }

  isLink(rel: string): boolean {
    return this.links[rel] !== undefined || this.actionLinks[rel] !== undefined;
  }

  deleteLink(rel: string) {
    delete this.links[rel];
    delete this.actionLinks[rel];
  }
}

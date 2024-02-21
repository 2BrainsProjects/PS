export class Link {
  constructor(
    public rel: string[],
    public href: string,
    public title: string | null = null,
    public type: string | null = null
  ) {}
}

export class Action {
  constructor(
    public name: string,
    public href: string,
    public method?: string,
    public type?: string,
    public fields?: Field[]
  ) {}
}

class Field {
  constructor(
    public name: string,
    public type: string,
    public value?: string,
    public title?: string
  ) {}
}

export enum Color {
  BLACK = 'B',
  WHITE = 'W',
}

declare global {
  interface String {
    toColor(): Color;
  }
}

String.prototype.toColor = function (): Color {
  const colors: Record<string, Color> = {
    B: Color.BLACK,
    W: Color.WHITE,
  };
  const turn: Color | undefined = colors[this.toUpperCase()];
  if (turn === undefined) {
    throw new Error(`Invalid Turn ${this}`);
  }
  return turn;
};

export function other(color: Color): Color {
  switch (color) {
    case Color.BLACK:
      return Color.WHITE;
    case Color.WHITE:
      return Color.BLACK;
  }
}

export {};

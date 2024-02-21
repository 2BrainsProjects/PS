export enum GameState {
  RUNNING = 'R',
  WHITE_WON = 'W',
  BLACK_WON = 'B',
  DRAW = 'D',
}

declare global {
  interface String {
    toGameState(): GameState;
  }
}

String.prototype.toGameState = function (): GameState {
  const states: Record<string, GameState> = {
    R: GameState.RUNNING,
    B: GameState.BLACK_WON,
    W: GameState.WHITE_WON,
    D: GameState.DRAW,
  };
  const state: GameState | undefined = states[this.toUpperCase()];
  if (state === undefined) {
    throw new Error(`Invalid GameState ${this}`);
  }
  return state;
};

export {};

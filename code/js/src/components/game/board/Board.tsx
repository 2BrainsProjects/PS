import React from 'react';
import './Board.css';
import { BoardModel } from '../../../domain/game/BoardModel';
import SquareView from './square/Square';

type BoardProps = {
  board: BoardModel;
  enabled: boolean;
};

function Board({ board, enabled }: BoardProps) {
  return (
    <div className={`Board ${enabled ? 'enabled' : ''}`}>
      {Array.from({ length: board.size }, (_, row) => (
        <div key={row} className="row">
          {Array.from({ length: board.size }, (_, col) => (
            <SquareView
              row={row + 1}
              col={col + 1}
              boardSize={board.size}
              piece={board.getPieceAt(row + 1, col + 1)}
              key={row + 1 + col + 1}
              enabled={enabled}
            />
          ))}
        </div>
      ))}
    </div>
  );
}

export default Board;

import React from 'react';
import { Piece } from '../../../../domain/game/Piece';
import './Piece.css';

type PieceProps = {
  piece: Piece | undefined;
};

function PieceView({ piece }: PieceProps) {
  return <div className="Piece" style={piece ? { background: Piece.getColor(piece) as string } : {}}></div>;
}

export default PieceView;

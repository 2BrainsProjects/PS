import { Piece } from '../../../../domain/game/Piece';
import PieceView from '../piece/Piece';
import './Square.css';
import React from 'react';
import { useLinks, useServices } from '../../../../contexts/ServicesContext';
import Rels from '../../../../services/Rels';
import { useErrorContext } from '../../../../contexts/ErrorContext';

type SquareProps = {
  row: number;
  col: number;
  boardSize: number;
  piece: Piece | undefined;
  enabled: boolean;
};

function SquareView({ row, col, piece, enabled }: SquareProps) {
  const services = useServices();
  const links = useLinks();
  const { setError } = useErrorContext();

  async function play() {
    if (!enabled || piece !== undefined) return;
    const link = links.getActionLink(Rels.PLAY_GAME);
    const result = await services.gamesService.play(link, row, col);
    result.onFailure(setError);
  }

  return (
    <div className="Square" onClick={play}>
      <div className="cross">
        <PieceView piece={piece}></PieceView>
      </div>
    </div>
  );
}

export default SquareView;

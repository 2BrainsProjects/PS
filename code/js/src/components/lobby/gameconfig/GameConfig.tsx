import React, { useState } from 'react';
import './GameConfig.css';
import OptionButton from './OptionButton';
import { useLinks, useServices } from '../../../contexts/ServicesContext';
import Rels from '../../../services/Rels';
import { useErrorContext } from '../../../contexts/ErrorContext';

const boardSizes = ['15', '19'];
const variants = ['Freestyle'];
const openings = ['Freestyle', 'Pro', 'Long_Pro'];

type GameConfigProps = {
  onJoinLobby: () => void;
};

function GameConfig({ onJoinLobby }: GameConfigProps) {
  const services = useServices();
  const links = useLinks();
  const [boardSize, setBoardSize] = useState('15');
  const [variant, setVariant] = useState('Freestyle');
  const [opening, setOpening] = useState('Freestyle');
  const { setError } = useErrorContext();

  async function joinLobby() {
    const link = links.getActionLink(Rels.JOIN_LOBBY);
    const result = await services.lobbyService.joinLobby(link, +boardSize, variant, opening);
    result.onSuccess(onJoinLobby).onFailure(setError);
  }

  return (
    <div className="GameConfig">
      <h1 className="title">Game Configuration</h1>
      <div className="container">
        <div className="option">
          <p>Board Size</p>
          <OptionButton options={boardSizes} setOption={setBoardSize} />
        </div>
        <div className="option">
          <p>Variant</p>
          <OptionButton options={variants} setOption={setVariant} />
        </div>
        <div className="option">
          <p>Opening</p>
          <OptionButton options={openings} setOption={setOpening} />
        </div>
        <button type="button" className="play-button" onClick={joinLobby}>
          Play
        </button>
      </div>
    </div>
  );
}

export default GameConfig;

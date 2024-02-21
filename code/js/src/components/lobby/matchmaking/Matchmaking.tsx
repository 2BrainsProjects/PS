import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Matchmaking.css';
import LoadingSpinner from '../../utils/spinner/LoadingSpinner';
import { useLinks, useServices } from '../../../contexts/ServicesContext';
import Rels from '../../../services/Rels';
import { useErrorContext } from '../../../contexts/ErrorContext';
import { Color } from '../../../domain/game/Color';

const POLLING_DELAY = 3000;

type MatchmakingProps = {
  onLeaveLobby: () => void;
};

function Matchmaking({ onLeaveLobby }: MatchmakingProps) {
  const services = useServices();
  const links = useLinks();
  const navigate = useNavigate();
  const { setError } = useErrorContext();

  async function joinGame(color: Color | undefined) {
    if (color) {
      const gameLink = links.getActionLink(Rels.GAME);
      if (gameLink === undefined) throw new Error('Game link is undefined');
      navigate(gameLink);
    }
  }
  async function leaveLobby() {
    const link = links.getActionLink(Rels.LEAVE_LOBBY);
    const result = await services.lobbyService.leaveLobby(link);
    result.onSuccess(onLeaveLobby).onFailure(setError);
  }

  function matchmakingPolling() {
    const link = links.getActionLink(Rels.FIND_MATCH);
    return setInterval(async () => {
      const result = await services.lobbyService.findMatch(link);
      result.onSuccess(joinGame).onFailure(setError);
    }, POLLING_DELAY);
  }

  useEffect(() => {
    const id = matchmakingPolling();
    return () => clearInterval(id);
  }, []);

  return (
    <div className="LoadingScreen">
      <div className="content">
        <p className="waiting-for-opponent">Waiting for opponent...</p>
        <LoadingSpinner />
        <button onClick={leaveLobby}>Cancel</button>
      </div>
    </div>
  );
}

export default Matchmaking;

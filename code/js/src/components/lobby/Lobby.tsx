import React, { useEffect, useState } from 'react';
import './Lobby.css';
import GameConfig from './gameconfig/GameConfig';
import Matchmaking from './matchmaking/Matchmaking';
import Rels from '../../services/Rels';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import { useNavigate } from 'react-router-dom';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';

function Lobby() {
  const [joinedLobby, setJoinedLobby] = useState<boolean>(undefined);
  const services = useServices();
  const links = useLinks();
  const navigate = useNavigate();

  async function reconnectIfInGame() {
    const link = links.getActionLink(Rels.FIND_MATCH);
    const result = await services.lobbyService.findMatch(link);
    result
      .onSuccess(color => {
        if (color) {
          const gameLink = links.getActionLink(Rels.GAME);
          if (gameLink === undefined) throw new Error('Game link is undefined');
          navigate(gameLink);
        }
        setJoinedLobby(true);
      })
      .onFailure(() => setJoinedLobby(false));
  }

  useEffect(() => {
    reconnectIfInGame();
  }, []);

  return joinedLobby === true ? (
    <Matchmaking onLeaveLobby={() => setJoinedLobby(false)} />
  ) : joinedLobby === false ? (
    <GameConfig onJoinLobby={() => setJoinedLobby(true)} />
  ) : (
    <LoadingSpinner />
  );
}

export default Lobby;

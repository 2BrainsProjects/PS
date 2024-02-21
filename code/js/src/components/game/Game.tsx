import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import './Game.css';
import Board from './board/Board';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import { GameStateModel, getStateMessage } from '../../domain/game/GameStateModel';
import Rels from '../../services/Rels';
import Player from './player/Player';
import { useNavigate } from 'react-router-dom';
import { useSession } from '../../contexts/SessionContext';
import { GameState } from '../../domain/game/GameState';
import { useErrorContext } from '../../contexts/ErrorContext';
import { PlayerModel } from '../../domain/game/Player';
import { Color, other } from '../../domain/game/Color';
import { parseURITemplate } from '../../services/Utils';

const POLLING_DELAY = 3000;

function Game() {
  const { gameId } = useParams();
  const services = useServices();
  const links = useLinks();
  const navigate = useNavigate();
  const { session, isLoggedIn } = useSession();
  const { setError } = useErrorContext();
  const [game, setGame] = useState<GameStateModel>();
  const [me, setMe] = useState<PlayerModel>();
  const [opponent, setOpponent] = useState<PlayerModel>();

  async function getGame() {
    const gameLink = parseURITemplate(links.getLink(Rels.GAME), { gameId });
    const game = await services.gamesService.getGame(gameLink);
    game.onSuccess(setGame).onFailure(setError);
    return game.getValueOrNull();
  }

  async function getPlayers() {
    await getGame(); // get game links
    const getPlayer = async (rel: string) => {
      const link = links.getActionLink(rel);
      return await services.usersService.getUser(link);
    };
    if (links.isLink(Rels.OPPONENT)) {
      const opponent = await getPlayer(Rels.OPPONENT);
      links.deleteLink(Rels.OPPONENT);
      opponent
        .onSuccess(op => {
          const myColor = links.getActionLink(Rels.BLACK_PLAYER) === session?.link ? Color.BLACK : Color.WHITE;
          setMe(new PlayerModel(session!, myColor));
          setOpponent(new PlayerModel(op, other(myColor)));
        })
        .onFailure(setError);
      return;
    }
    const blackPlayer = await getPlayer(Rels.BLACK_PLAYER);
    const whitePlayer = await getPlayer(Rels.WHITE_PLAYER);
    blackPlayer
      .onSuccess(black => {
        whitePlayer
          .onSuccess(white => {
            if (session?.link === white.link) {
              setMe(new PlayerModel(white, Color.WHITE));
              setOpponent(new PlayerModel(black, Color.BLACK));
            } else {
              setMe(new PlayerModel(black, Color.BLACK));
              setOpponent(new PlayerModel(white, Color.WHITE));
            }
          })
          .onFailure(setError);
      })
      .onFailure(setError);
  }

  async function leaveGame() {
    const link = links.getActionLink(Rels.LEAVE_GAME);
    const result = await services.gamesService.leaveGame(link);
    result.onFailure(setError).onSuccess(() => navigate('/'));
  }

  function gamePolling() {
    const id = setInterval(async () => {
      const game = await getGame();
      if (game?.state !== undefined && game?.state !== GameState.RUNNING) {
        clearInterval(id);
      }
    }, POLLING_DELAY);
    return id;
  }

  useEffect(() => {
    const id = gamePolling();
    getPlayers();
    return () => clearInterval(id);
  }, []);

  const isPlaying = isLoggedIn && me?.user.link === session?.link && game?.state === GameState.RUNNING;
  return (
    <div className="Game">
      {game !== undefined && me !== undefined && opponent !== undefined ? (
        <div className="content">
          <div className="left">
            <Player player={opponent} />
            <Board board={game.board} enabled={isPlaying && game?.turn === me?.color} />
            <Player player={me} />
          </div>
          <div className="right">
            <div className="state">
              <p>{getStateMessage(game, me, opponent)}</p>
            </div>
            {isPlaying ? <button onClick={leaveGame}>Leave Game</button> : null}
          </div>
        </div>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}
export default Game;

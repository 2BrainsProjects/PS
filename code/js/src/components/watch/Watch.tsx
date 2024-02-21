import React, { useEffect, useState } from 'react';
import './Watch.css';
import { Link, useSearchParams } from 'react-router-dom';
import { GameModel } from '../../domain/game/GameModel';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import Rels from '../../services/Rels';
import { useErrorContext } from '../../contexts/ErrorContext';
import { GameState } from '../../domain/game/GameState';
import Pagination from '../utils/pagination/Pagination';

const MAX_GAMES_PER_PAGE = 10;

function Watch() {
  const services = useServices();
  const links = useLinks();
  const [games, setGames] = useState<Array<GameModel>>();
  const { setError } = useErrorContext();
  const [searchParams, _] = useSearchParams();

  async function getGames(page?: number) {
    const link = links.getActionLink(Rels.GAMES);
    const games = await services.gamesService.getGames(link, undefined, GameState.RUNNING, page, MAX_GAMES_PER_PAGE);
    games.onSuccess(setGames).onFailure(setError);
  }

  useEffect(() => {
    const page = parseInt(searchParams.get('page') ?? '1');
    getGames(page);
  }, []);

  return (
    <div className="Watch">
      {games !== undefined ? (
        <>
          <div className="title">
            <h1>Watch Live</h1>
          </div>
          <div className="content">
            {games.length > 0 ? (
              games.map((game, index) => (
                <Link style={{ textDecoration: 'none' }} to={`${game.link}`} key={index}>
                  <div className="game">
                    {game.black.name} ({game.black.stats.rating}
                    )&nbsp;&nbsp;vs&nbsp;&nbsp;
                    {game.white.name} ({game.white.stats.rating})
                  </div>
                </Link>
              ))
            ) : (
              <p>No games happening right now</p>
            )}
          </div>
          <Pagination
            onPageChange={async page => {
              await getGames(page);
            }}
            hasPrevious={games.length > 0}
            hasNext={games.length === MAX_GAMES_PER_PAGE}
          />
        </>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}

export default Watch;

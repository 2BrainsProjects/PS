import * as React from 'react';
import { useEffect, useState } from 'react';
import { User } from '../../domain/user/User';
import './Profile.css';
import { useNavigate, useParams } from 'react-router-dom';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';
import { GameModel } from '../../domain/game/GameModel';
import { useSession } from '../../contexts/SessionContext';
import { Color } from '../../domain/game/Color';
import { useErrorContext } from '../../contexts/ErrorContext';
import { Result } from '../../services/Result';
import { parseURITemplate } from '../../services/Utils';
import Rels from '../../services/Rels';

const MAX_GAMES_IN_PROFILE = 5;

function Profile() {
  const { userId } = useParams();
  const services = useServices();
  const links = useLinks();
  const navigate = useNavigate();
  const { session } = useSession();
  const { setError } = useErrorContext();
  const [user, setUser] = useState<User>();
  const [games, setGames] = useState<Array<GameModel>>();
  useEffect(() => {
    getProfile();
  }, []);

  async function getProfile() {
    const userLink = links.getLink(Rels.USER);
    const result =
      userId !== undefined
        ? await services.usersService.getUser(parseURITemplate(userLink, { userId }))
        : new Result(session);

    result.onFailure(setError).onSuccess(async user => {
      setUser(user);
      const gamesLink = links.getActionLink(Rels.GAMES);
      const games = await services.gamesService.getGames(
        gamesLink,
        user!.name,
        undefined,
        undefined,
        MAX_GAMES_IN_PROFILE
      );
      games.onFailure(setError).onSuccess(setGames);
    });
  }

  return (
    <div className="Profile">
      {user !== undefined && games !== undefined ? (
        <>
          <div className="user">
            <div className="avatar">{user.name[0].toUpperCase()}</div>
            <div className="info">
              <div className="name">{user.name}</div>
              <div className="email">{user.email}</div>
            </div>
          </div>
          <div className="stats">
            <div>
              <p>Rating</p>
              <p>{user.stats.rating}</p>
            </div>
            <div>
              <p>Games Played</p>
              <p>{user.stats.gamesPlayed}</p>
            </div>
            <div>
              <p>Wins</p>
              <p>{user.stats.wins}</p>
            </div>
            <div>
              <p>Draws</p>
              <p>{user.stats.draws}</p>
            </div>
            <div>
              <p>Losses</p>
              <p>{user.stats.losses}</p>
            </div>
          </div>
          <div className="recent-games">
            {games.length > 0 ? (
              <>
                <h3>Recent Games</h3>
                {games?.map((game, idx) => {
                  const opponent = game.opponent === Color.BLACK ? game.black : game.white;
                  return (
                    <p key={idx} onClick={() => navigate(`${game.link}`)}>
                      {opponent.name}
                    </p>
                  );
                })}
              </>
            ) : (
              <h3>No games played</h3>
            )}
          </div>
        </>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}

export default Profile;

import React, { useEffect, useState } from 'react';
import './Leaderboard.css';
import { User } from '../../domain/user/User';
import LoadingSpinner from '../utils/spinner/LoadingSpinner';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import Rels from '../../services/Rels';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useErrorContext } from '../../contexts/ErrorContext';
import Pagination from '../utils/pagination/Pagination';

const MAX_USERS_PER_PAGE = 10;

function LeaderBoard() {
  const services = useServices();
  const links = useLinks();
  const navigate = useNavigate();
  const [users, setUsers] = useState<Array<User>>();
  const { setError } = useErrorContext();
  const [searchParams, _] = useSearchParams();
  const [page, setPage] = useState(1);

  async function getLeaderboard(page?: number) {
    const link = links.getActionLink(Rels.USERS);
    const users = await services.usersService.getUsers(link, page, MAX_USERS_PER_PAGE, 'rating', 'desc');
    users.onSuccess(setUsers).onFailure(setError);
  }

  useEffect(() => {
    const page = parseInt(searchParams.get('page') ?? '1');
    setPage(page);
    getLeaderboard(page);
  }, []);

  return (
    <div className="Leaderboard">
      {users !== undefined ? (
        <>
          <h1 className="title">Leaderboard</h1>
          <div className="container">
            <div className="header">
              <h3>Rank</h3>
              <h3>Name</h3>
              <h3>Rating</h3>
            </div>
            {users.length > 0 ? (
              users.map((user, index) => (
                <div
                  className="user"
                  key={index}
                  onClick={() => {
                    const userLink = user.link;
                    if (userLink === undefined) throw new Error('User link is undefined');
                    navigate(userLink);
                  }}
                >
                  <h3 className="rank">#{index + 1 + (page - 1) * MAX_USERS_PER_PAGE}</h3>
                  <h3 className="username">{user.name}</h3>
                  <h3 className="rating">{user.stats.rating}</h3>
                </div>
              ))
            ) : (
              <p>No users</p>
            )}
            <Pagination
              onPageChange={async page => {
                setPage(page);
                await getLeaderboard(page);
              }}
              hasPrevious={users.length > 0}
              hasNext={users.length === MAX_USERS_PER_PAGE}
            />
          </div>
        </>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}

export default LeaderBoard;

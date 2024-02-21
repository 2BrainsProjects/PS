import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Player.css';
import { PlayerModel } from '../../../domain/game/Player';

type PlayerProps = {
  player: PlayerModel;
};

function Player({ player }: PlayerProps) {
  const navigate = useNavigate();
  const { user, color } = player;
  return (
    <div className="Player">
      <div className="avatar">{user.name[0].toUpperCase()}</div>
      <div className="info">
        <p className="link" onClick={() => navigate(`${user?.link}`)}>
          {user.name} ({user.stats.rating})
        </p>
        <p className="stats">
          {user.stats.wins}W {user.stats.losses}L {user.stats.draws}D
        </p>
      </div>
      <div className="color">
        <i className={color.toString()}></i>
      </div>
    </div>
  );
}

export default Player;

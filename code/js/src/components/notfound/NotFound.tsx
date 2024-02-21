import React from 'react';
import './NotFound.css';
import { useNavigate } from 'react-router-dom';

function NotFound() {
  const navigate = useNavigate();
  return (
    <div className="NotFound">
      <div className="title">
        <h1>404</h1>
      </div>
      <div className="content">
        <p>Page not found</p>
        <button onClick={() => navigate('/')}>Back to home page</button>
      </div>
    </div>
  );
}

export default NotFound;

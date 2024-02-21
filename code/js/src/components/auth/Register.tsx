import React, { useState } from 'react';
import './Auth.css';
import { Link, useNavigate } from 'react-router-dom';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import Rels from '../../services/Rels';
import { useErrorContext } from '../../contexts/ErrorContext';
import { validateRegister } from './Utils';

function Register() {
  const services = useServices();
  const navigate = useNavigate();
  const links = useLinks();
  const { setError } = useErrorContext();
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  async function register() {
    const problem = validateRegister(username, email, password, confirmPassword);
    if (problem) {
      setError(problem);
      return;
    }
    const link = links.getActionLink(Rels.REGISTER);
    const result = await services.usersService.register(link, username, email, password);
    result.onSuccess(() => navigate('/login')).onFailure(setError);
  }

  return (
    <div className="Auth">
      <h1 className="title">Register</h1>
      <div className="container">
        <form className="form">
          <input
            type="text"
            id="username"
            name="Username"
            placeholder="Username"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
          <input
            type="text"
            id="email"
            name="Email"
            placeholder="Email"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
          />
          <input
            type="password"
            id="password"
            name="Password"
            placeholder="Password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
          <input
            type="password"
            id="confirm-password"
            name="Confirm Password"
            placeholder="Confirm Password"
            value={confirmPassword}
            onChange={e => setConfirmPassword(e.target.value)}
            required
          />
          <h2 className="alternative">
            Already have an account? <Link to="/login">Login</Link>
          </h2>
          <button
            type="submit"
            className="confirm-button"
            onClick={e => {
              e.preventDefault();
              register();
            }}
          >
            Register
          </button>
        </form>
      </div>
    </div>
  );
}

export default Register;

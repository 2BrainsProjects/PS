import React, { useState } from 'react';
import './Auth.css';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useSession } from '../../contexts/SessionContext';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import Rels from '../../services/Rels';
import { useErrorContext } from '../../contexts/ErrorContext';
import { validateLogin } from './Utils';

function Login() {
  const services = useServices();
  const navigate = useNavigate();
  const links = useLinks();
  const location = useLocation();
  const { setError } = useErrorContext();
  const { login } = useSession();
  const [usernameOrEmail, setUsernameOrEmail] = useState('');
  const [password, setPassword] = useState('');

  async function loginUser() {
    const problem = validateLogin(usernameOrEmail, password);
    if (problem) {
      setError(problem);
      return;
    }
    const [name, email] = usernameOrEmail.includes('@') ? [undefined, usernameOrEmail] : [usernameOrEmail, undefined];
    const link = links.getActionLink(Rels.LOGIN);
    const result = await services.usersService.login(link, name, email, password);
    result.onFailure(setError).onSuccess(async () => {
      const userHomeLink = links.getLink(Rels.USER_HOME);
      const user = await services.usersService.getUserHome(userHomeLink);
      user.onFailure(setError).onSuccess(user => {
        const userLink = links.getActionLink(Rels.USER);
        login({
          name: user.name,
          email: user.email,
          stats: user.stats,
          link: userLink,
        });
        navigate(location.state?.link || '/', { replace: true });
      });
    });
  }

  return (
    <div className="Auth">
      <h1 className="title">Login</h1>
      <div className="container">
        <form className="form">
          <input
            type="text"
            id="username-or-email"
            name="Username or E-mail"
            placeholder="Username or E-mail"
            value={usernameOrEmail}
            onChange={e => setUsernameOrEmail(e.target.value)}
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
          <h2 className="alternative">
            Don&apos;t have an account? <Link to="/register">Register</Link>
          </h2>
          <button
            type="submit"
            className="confirm-button"
            onClick={async e => {
              e.preventDefault();
              await loginUser();
            }}
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;

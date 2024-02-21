import React, { useEffect, useState } from 'react';
import './App.css';
import Sidebar from '../components/sidebar/Sidebar';
import Home from '../components/home/Home';
import Lobby from '../components/lobby/Lobby';
import Watch from '../components/watch/Watch';
import Leaderboard from '../components/leaderboard/Leaderboard';
import Login from '../components/auth/Login';
import Register from '../components/auth/Register';
import Game from '../components/game/Game';
import { useSession } from '../contexts/SessionContext';
import Profile from '../components/profile/Profile';
import Rels from '../services/Rels';
import LoadingSpinner from '../components/utils/spinner/LoadingSpinner';
import { useLinks, useServices } from '../contexts/ServicesContext';
import NotFound from '../components/notfound/NotFound';
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useErrorContext } from '../contexts/ErrorContext';

function App() {
  const services = useServices();
  const links = useLinks();
  const { isLoggedIn, login } = useSession();
  const { setError } = useErrorContext();
  const [loaded, setLoaded] = useState<boolean>(false);

  useEffect(() => {
    async function loadLinks() {
      const home = await services.homeService.getHome();
      if (home.isFailure()) {
        setError(home.getProblem());
        return;
      }
      const userHomeLink = links.getLink(Rels.USER_HOME);
      const userHome = await services.usersService.getUserHome(userHomeLink);
      userHome.onSuccess(user => {
        const link = links.getActionLink(Rels.USER);
        const { name, email, stats } = user;
        login({ name, email, stats, link });
      });
      setLoaded(true);
    }
    loadLinks();
  }, []);

  function AuthenticatedRoute({ children }: { children: React.ReactElement }) {
    const location = useLocation();
    if (!isLoggedIn) return <Navigate to={'/login'} replace state={{ link: location.pathname }} />;
    return children;
  }

  return (
    <div className="App">
      {loaded ? (
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Sidebar />}>
              <Route index element={<Home />} />
              <Route
                path="/me"
                element={
                  <AuthenticatedRoute>
                    <Profile />
                  </AuthenticatedRoute>
                }
              />
              <Route path="/user/:userId" element={<Profile />} />
              <Route path="/game/:gameId" element={<Game />} />
              <Route
                path="/lobby"
                element={
                  <AuthenticatedRoute>
                    <Lobby />
                  </AuthenticatedRoute>
                }
              />
              <Route path="/watch" element={<Watch />} />
              <Route path="/leaderboard" element={<Leaderboard />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </BrowserRouter>
      ) : (
        <LoadingSpinner />
      )}
    </div>
  );
}

export default App;

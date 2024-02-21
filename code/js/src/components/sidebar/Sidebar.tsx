import React from 'react';
import { Link, Outlet } from 'react-router-dom';
import './Sidebar.css';
import { useSession } from '../../contexts/SessionContext';
import { useLinks, useServices } from '../../contexts/ServicesContext';
import Rels from '../../services/Rels';
import { useErrorContext } from '../../contexts/ErrorContext';

function Sidebar() {
  const services = useServices();
  const links = useLinks();
  const { session, isLoggedIn, logout } = useSession();
  const { setError } = useErrorContext();

  async function logoutUser() {
    if (!isLoggedIn) throw new Error('User is not logged in');
    const link = links.getActionLink(Rels.LOGOUT);
    const result = await services.usersService.logout(link);
    result.onSuccess(logout).onFailure(setError);
  }

  return (
    <>
      <div className="Sidebar">
        <div className="header">
          {isLoggedIn ? (
            <Link to="/me" className="profile">
              <div className="avatar">{session?.name[0].toUpperCase()}</div>
              <div className="name">{session?.name}</div>
            </Link>
          ) : null}
        </div>
        <div className="menu">
          {sidebarNavItems.map((item, index) => {
            if (item.authenticated && !isLoggedIn) return null;
            if (item.nonAuthenticated && isLoggedIn) return null;
            return (
              <Link
                to={item.href}
                key={index}
                className="item"
                onClick={() => (index === sidebarNavItems.length - 1 ? logoutUser() : null)}
              >
                <div className="icon">
                  <i className={item.icon}></i>
                </div>
                <div className="name">{item.name}</div>
              </Link>
            );
          })}
        </div>
      </div>
      <Outlet />
    </>
  );
}

export default Sidebar;

interface SidebarNavItem {
  name: string;
  icon: string;
  href: string;
  authenticated?: boolean;
  nonAuthenticated?: boolean;
}

const sidebarNavItems: SidebarNavItem[] = [
  {
    name: 'Login',
    icon: 'fa fa-sign-in',
    href: '/login',
    nonAuthenticated: true,
  },
  {
    name: 'Home',
    icon: 'fa fa-home',
    href: '/',
  },
  {
    name: 'Play',
    icon: 'fa fa-play',
    href: '/lobby',
    authenticated: true,
  },
  {
    name: 'Watch',
    icon: 'fa fa-binoculars',
    href: '/watch',
  },
  {
    name: 'Leaderboard',
    icon: 'fa fa-trophy',
    href: '/leaderboard',
  },
  {
    name: 'Logout',
    icon: 'fa fa-sign-out',
    href: '/',
    authenticated: true,
  },
];

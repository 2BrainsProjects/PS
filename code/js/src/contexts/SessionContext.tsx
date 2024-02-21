import * as React from 'react';
import { useState, createContext, useContext } from 'react';
import { Stats } from '../domain/user/Stats';

class Session {
  constructor(
    public name: string,
    public email: string,
    public stats: Stats,
    public link: string
  ) {}
}

type LoginType = {
  name: string;
  email: string;
  stats: Stats;
  link: string;
};

type SessionContextType = {
  isLoggedIn: boolean;
  session: Session | undefined;
  login: (userInfo: LoginType) => void;
  logout: () => void;
};

const SessionContext = createContext<SessionContextType>({
  session: undefined,
  isLoggedIn: false,
  login: () => {},
  logout: () => {},
});

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<Session>();
  const [isLoggedIn, setLoggedIn] = useState(session !== undefined);

  function login({ name, email, stats, link }: LoginType) {
    setSession(new Session(name, email, stats, link));
    setLoggedIn(true);
  }
  function logout() {
    setSession(undefined);
    setLoggedIn(false);
  }

  return (
    <SessionContext.Provider value={{ session: session, isLoggedIn, login, logout }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession(): SessionContextType {
  return useContext(SessionContext);
}

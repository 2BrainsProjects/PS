import * as React from 'react';
import { useState, createContext, useContext } from 'react';
import { GomokuService } from '../services/GomokuService';
import { LinksService } from '../services/LinksService';
import { HttpService } from '../services/HttpService';

const linksService = new LinksService();
const http = new HttpService((links, actionLinks) => linksService.updateLinks(links, actionLinks));

type ServicesContextType = {
  services: GomokuService;
};

const ServicesContext = createContext<ServicesContextType | undefined>(undefined);

export function GomokuServiceProvider({ children }: { children: React.ReactNode }) {
  const [services] = useState(new GomokuService(http));
  return <ServicesContext.Provider value={{ services }}>{children}</ServicesContext.Provider>;
}

export function useServices(): GomokuService {
  const context = useContext(ServicesContext);
  if (context === undefined) {
    throw new Error('useServices must be used within a ServicesProvider');
  }
  return context.services;
}

export function useLinks(): LinksService {
  return linksService;
}

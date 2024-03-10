import * as React from 'react';
import { useState, createContext, useContext } from 'react';
import { AnonichatService } from '../services/AnonichatService';
import { LinksService } from '../services/LinksService';
import { HttpService } from '../services/HttpService';

const linksService = new LinksService();
const http = new HttpService((links, actionLinks) => linksService.updateLinks(links, actionLinks));

type ServicesContextType = {
  services: AnonichatService;
};

const ServicesContext = createContext<ServicesContextType | undefined>(undefined);

export function AnonichatServiceProvider({ children }: { children: React.ReactNode }) {
  const [services] = useState(new AnonichatService(http));
  return <ServicesContext.Provider value={{ services }}>{children}</ServicesContext.Provider>;
}

export function useServices(): AnonichatService {
  const context = useContext(ServicesContext);
  if (context === undefined) {
    throw new Error('useServices must be used within a ServicesProvider');
  }
  return context.services;
}

export function useLinks(): LinksService {
  return linksService;
}

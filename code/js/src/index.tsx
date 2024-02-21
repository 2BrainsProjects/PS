import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './app/App';
import { SessionProvider } from './contexts/SessionContext';
import { GomokuServiceProvider } from './contexts/ServicesContext';
import './index.css';
import { ErrorHandler } from './contexts/ErrorContext';
import ErrorPopup from './components/utils/error-popup/ErrorPoup';

const root = ReactDOM.createRoot(document.getElementById('root') as HTMLElement);
root.render(
  <ErrorHandler>
    <SessionProvider>
      <GomokuServiceProvider>
        <ErrorPopup />
        <App />
      </GomokuServiceProvider>
    </SessionProvider>
  </ErrorHandler>
);

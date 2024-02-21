import * as React from 'react';
import { useState, createContext, useContext } from 'react';
import { Problem } from '../services/media/problem/Problem';

type ErrorType = {
  title: string;
  detail: string;
};

type ErrorContextType = {
  error: ErrorType | undefined;
  setError: (problem: Problem) => void;
};

const ErrorContext = createContext<ErrorContextType>({
  error: undefined,
  setError: () => {},
});

export function ErrorHandler({ children }: { children: React.ReactNode }) {
  const [error, setError] = useState<ErrorType | undefined>();
  return (
    <ErrorContext.Provider
      value={{
        error,
        setError: (problem: Problem) => {
          setError({
            title: problem.title ?? 'Error',
            detail: problem.detail ?? 'An unknown error occurred',
          });
          setTimeout(() => setError(undefined), 5000);
        },
      }}
    >
      {children}
    </ErrorContext.Provider>
  );
}

export function useErrorContext(): ErrorContextType {
  return useContext(ErrorContext);
}

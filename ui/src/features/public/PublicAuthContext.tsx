/* eslint-disable react-refresh/only-export-components */
import { createContext, useCallback, useContext, useState } from 'react';

import { clearPublicToken,getPublicToken, setPublicToken as persistToken } from './auth';

type PublicAuthContextValue = {
  hasToken: boolean;
  setToken: (token: string) => void;
  clearToken: () => void;
};

const PublicAuthContext = createContext<PublicAuthContextValue | null>(null);

export function PublicAuthProvider({ children }: { children: React.ReactNode }) {
  const [hasToken, setHasToken] = useState(() => !!getPublicToken());

  const setToken = useCallback((token: string) => {
    persistToken(token);
    setHasToken(true);
  }, []);

  const clearToken = useCallback(() => {
    clearPublicToken();
    setHasToken(false);
  }, []);

  return (
    <PublicAuthContext.Provider value={{ hasToken, setToken, clearToken }}>
      {children}
    </PublicAuthContext.Provider>
  );
}

export function usePublicAuth() {
  const ctx = useContext(PublicAuthContext);
  if (!ctx) throw new Error('usePublicAuth must be used within PublicAuthProvider');
  return ctx;
}

export function useOptionalPublicAuth(): PublicAuthContextValue | null {
  return useContext(PublicAuthContext);
}

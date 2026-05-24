import { createContext, useContext, useMemo, useState } from 'react';
import { clearStoredAuth, getStoredUser, setStoredAuth } from '../api/client';
import * as authApi from '../api/authApi';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => getStoredUser());
  const [loading, setLoading] = useState(false);

  async function login(credentials) {
    setLoading(true);
    try {
      const response = await authApi.login(credentials);
      setStoredAuth(response);
      setUser(response.user);
      return response;
    } finally {
      setLoading(false);
    }
  }

  async function register(payload) {
    setLoading(true);
    try {
      const response = await authApi.register(payload);
      setStoredAuth(response);
      setUser(response.user);
      return response;
    } finally {
      setLoading(false);
    }
  }

  function logout() {
    clearStoredAuth();
    setUser(null);
  }

  const value = useMemo(() => ({ user, loading, login, register, logout, isAuthenticated: Boolean(user) }), [user, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

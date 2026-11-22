import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authService } from '../services/authService';
import { userService } from '../services/userService';
import { storage } from '../lib/storage';
import type { LoginRequest, RegisterRequest, Role, User } from '../types';

interface AuthContextValue {
  user: User | null;
  initializing: boolean;
  isAuthenticated: boolean;
  login: (payload: LoginRequest) => Promise<User>;
  register: (payload: RegisterRequest) => Promise<User>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => storage.getUser<User>());
  const [initializing, setInitializing] = useState<boolean>(() => Boolean(storage.getToken()));

  useEffect(() => {
    const token = storage.getToken();
    if (!token) {
      setInitializing(false);
      return;
    }
    userService
      .me()
      .then((profile) => {
        setUser(profile);
        storage.setUser(profile);
      })
      .catch(() => {
        storage.clear();
        setUser(null);
      })
      .finally(() => setInitializing(false));
  }, []);

  const applySession = useCallback((token: string, profile: User) => {
    storage.setToken(token);
    storage.setUser(profile);
    setUser(profile);
  }, []);

  const login = useCallback(
    async (payload: LoginRequest) => {
      const result = await authService.login(payload);
      applySession(result.token, result.user);
      return result.user;
    },
    [applySession],
  );

  const register = useCallback(
    async (payload: RegisterRequest) => {
      const result = await authService.register(payload);
      applySession(result.token, result.user);
      return result.user;
    },
    [applySession],
  );

  const logout = useCallback(async () => {
    try {
      await authService.logout();
    } catch {
      // El cierre de sesión local siempre debe completarse.
    }
    storage.clear();
    setUser(null);
  }, []);

  const refreshUser = useCallback(async () => {
    const profile = await userService.me();
    setUser(profile);
    storage.setUser(profile);
  }, []);

  const hasRole = useCallback(
    (...roles: Role[]) => (user ? roles.includes(user.role) : false),
    [user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      initializing,
      isAuthenticated: Boolean(user),
      login,
      register,
      logout,
      refreshUser,
      hasRole,
    }),
    [user, initializing, login, register, logout, refreshUser, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return context;
}

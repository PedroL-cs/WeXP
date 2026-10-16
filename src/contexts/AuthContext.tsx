import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react';
import { AUTH_EXPIRED_EVENT } from '../api/apiFetch';

interface AuthContextData {
  isAuthenticated: boolean;
  login: (token: string, rememberMe: boolean) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextData | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [isAuthenticated, setIsAuthenticated] = useState(() => {
    return (
      localStorage.getItem('token') !== null ||
      sessionStorage.getItem('token') !== null
    );
  });

  useEffect(() => {
    function handleExpiredSession() {
      setIsAuthenticated(false);
    }

    window.addEventListener(AUTH_EXPIRED_EVENT, handleExpiredSession);
    return () => {
      window.removeEventListener(AUTH_EXPIRED_EVENT, handleExpiredSession);
    };
  }, []);

  function login(token: string, rememberMe: boolean) {
    if (rememberMe) {
      localStorage.setItem('token', token);
      sessionStorage.removeItem('token');
    } else {
      sessionStorage.setItem('token', token);
      localStorage.removeItem('token');
    }

    setIsAuthenticated(true);
  }

  function logout() {
    localStorage.removeItem('token');
    sessionStorage.removeItem('token');

    setIsAuthenticated(false);
  }

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// The hook intentionally shares this module with its provider.
// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth deve ser usado dentro de um AuthProvider');
  }

  return context;
}

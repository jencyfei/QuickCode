import { useCallback, useEffect, useState } from 'react';
import { User } from '@/types';
import { authService } from '@/services/auth';

export interface UseAuthReturn {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, name: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

export const useAuth = (): UseAuthReturn => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 初始化：从localStorage恢复token和user
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
  }, []);

  // 登录
  const login = useCallback(async (email: string, password: string) => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await authService.login({ email, password });
      const { user: userData, token: newToken } = response.data;

      setUser(userData);
      setToken(newToken);

      // 保存到localStorage
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch (err) {
      const message = err instanceof Error ? err.message : '登录失败';
      setError(message);
      throw err;
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 注册
  const register = useCallback(
    async (email: string, password: string, name: string) => {
      setIsLoading(true);
      setError(null);

      try {
        const response = await authService.register({
          email,
          password,
          name,
          confirmPassword: password,
        });
        const { user: userData, token: newToken } = response.data;

        setUser(userData);
        setToken(newToken);

        // 保存到localStorage
        localStorage.setItem('token', newToken);
        localStorage.setItem('user', JSON.stringify(userData));
      } catch (err) {
        const message = err instanceof Error ? err.message : '注册失败';
        setError(message);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  // 登出
  const logout = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      await authService.logout();
    } catch (err) {
      console.error('登出失败:', err);
    } finally {
      setUser(null);
      setToken(null);
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      setIsLoading(false);
    }
  }, []);

  // 清除错误
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  return {
    user,
    token,
    isLoading,
    isAuthenticated: !!token && !!user,
    error,
    login,
    register,
    logout,
    clearError,
  };
};

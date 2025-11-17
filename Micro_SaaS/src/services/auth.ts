import apiClient from './api';
import { User, ApiResponse } from '@/types';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  name: string;
  confirmPassword: string;
}

export interface AuthResponse {
  user: User;
  token: string;
}

export const authService = {
  // 登录
  login: (data: LoginRequest): Promise<ApiResponse<AuthResponse>> =>
    apiClient.post('/auth/login', data),

  // 注册
  register: (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> =>
    apiClient.post('/auth/register', data),

  // 登出
  logout: (): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/logout'),

  // 获取当前用户信息
  getCurrentUser: (): Promise<ApiResponse<User>> =>
    apiClient.get('/auth/me'),

  // 刷新token
  refreshToken: (): Promise<ApiResponse<{ token: string }>> =>
    apiClient.post('/auth/refresh'),

  // 修改密码
  changePassword: (data: {
    oldPassword: string;
    newPassword: string;
    confirmPassword: string;
  }): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/change-password', data),

  // 忘记密码
  forgotPassword: (email: string): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/forgot-password', { email }),

  // 重置密码
  resetPassword: (data: {
    token: string;
    password: string;
    confirmPassword: string;
  }): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/reset-password', data),

  // 验证邮箱
  verifyEmail: (token: string): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/verify-email', { token }),

  // 重新发送验证邮件
  resendVerificationEmail: (email: string): Promise<ApiResponse<void>> =>
    apiClient.post('/auth/resend-verification', { email }),
};

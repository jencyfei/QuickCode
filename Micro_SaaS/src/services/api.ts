import axios, { AxiosInstance, AxiosError } from 'axios';
import { ApiResponse } from '@/types';

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/v1',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT || '10000'),
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => {
    // 直接返回数据
    return response.data;
  },
  (error: AxiosError<ApiResponse>) => {
    // 处理错误
    if (error.response?.status === 401) {
      // 清除token并重定向到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
    }

    // 返回错误信息
    const message = error.response?.data?.message || error.message || '请求失败';
    const apiError = new Error(message);
    return Promise.reject(apiError);
  }
);

export default apiClient;

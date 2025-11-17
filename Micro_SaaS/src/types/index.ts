// 通用类型定义

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface PaginationParams {
  page: number;
  pageSize: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

export interface User {
  id: string;
  email: string;
  name: string;
  avatar?: string;
  role: 'admin' | 'user';
  createdAt: string;
  updatedAt: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
}

export interface Template {
  id: string;
  name: string;
  description: string;
  category: string;
  thumbnail?: string;
  chartTypes: string[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface Upload {
  id: string;
  fileName: string;
  fileSize: number;
  fileType: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  uploadedAt: string;
  userId: string;
}

export interface Analysis {
  id: string;
  uploadId: string;
  templateId: string;
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  results?: AnalysisResult[];
  insights?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface AnalysisResult {
  id: string;
  analysisId: string;
  chartType: string;
  chartData: any;
  title: string;
  description?: string;
}

export interface ChartConfig {
  type: 'line' | 'bar' | 'pie' | 'area' | 'scatter';
  title: string;
  xAxis: string;
  yAxis: string;
  data: any[];
}

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
  duration?: number;
}

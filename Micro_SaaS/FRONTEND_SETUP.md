# 前端项目搭建指南 - 第2周周一

**目标**: 搭建React + TypeScript + TailwindCSS项目骨架  
**时间**: 6小时  
**输出**: 可运行的前端项目

---

## 📋 快速开始（5分钟）

```bash
# 1. 创建项目
npm create vite@latest dataviz-frontend -- --template react-ts

# 2. 进入项目
cd dataviz-frontend

# 3. 安装依赖
npm install

# 4. 启动开发服务器
npm run dev

# 访问: http://localhost:5173
```

---

## 🛠️ 详细搭建步骤

### 步骤1: 创建Vite项目（30分钟）

```bash
# 创建项目
npm create vite@latest dataviz-frontend -- --template react-ts

# 进入项目目录
cd dataviz-frontend

# 安装依赖
npm install

# 验证项目
npm run dev
```

**项目结构**:
```
dataviz-frontend/
├── src/
│   ├── App.tsx
│   ├── App.css
│   ├── main.tsx
│   └── vite-env.d.ts
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── .gitignore
```

---

### 步骤2: 安装TailwindCSS（30分钟）

```bash
# 安装TailwindCSS和依赖
npm install -D tailwindcss postcss autoprefixer

# 初始化TailwindCSS
npx tailwindcss init -p
```

**修改 tailwind.config.js**:
```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#EEF2FF',
          100: '#E0E7FF',
          200: '#C7D2FE',
          300: '#A5B4FC',
          400: '#818CF8',
          500: '#6366F1',
          600: '#4F46E5',
          700: '#4338CA',
          800: '#3730A3',
          900: '#312E81',
        },
        secondary: {
          50: '#FDF2F8',
          100: '#FCE7F3',
          200: '#FBCFE8',
          300: '#F8B4D6',
          400: '#F472B6',
          500: '#EC4899',
          600: '#DB2777',
          700: '#BE185D',
          800: '#9D174D',
          900: '#831843',
        },
      },
      fontFamily: {
        display: ['Poppins', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        code: ['Fira Code', 'monospace'],
      },
    },
  },
  plugins: [],
}
```

**修改 src/index.css**:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* 自定义样式 */
@layer base {
  body {
    @apply bg-gray-50 text-gray-900;
  }
}

@layer components {
  .btn-primary {
    @apply px-4 py-2 bg-primary-500 text-white rounded-lg hover:bg-primary-600 transition;
  }
  
  .btn-secondary {
    @apply px-4 py-2 bg-gray-100 text-gray-900 rounded-lg hover:bg-gray-200 transition;
  }
  
  .input-base {
    @apply w-full px-3 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500;
  }
  
  .card {
    @apply bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition;
  }
}
```

---

### 步骤3: 安装必要依赖（30分钟）

```bash
# HTTP客户端
npm install axios

# 状态管理
npm install zustand

# 数据查询
npm install @tanstack/react-query

# 图表库
npm install recharts

# 路由
npm install react-router-dom

# 日期处理
npm install dayjs

# 工具库
npm install clsx lodash-es

# 开发依赖
npm install -D @types/react @types/react-dom @types/node
npm install -D typescript
```

**package.json示例**:
```json
{
  "name": "dataviz-frontend",
  "private": true,
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "lint": "eslint src --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "type-check": "tsc --noEmit"
  },
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "axios": "^1.6.0",
    "zustand": "^4.4.0",
    "@tanstack/react-query": "^5.0.0",
    "recharts": "^2.10.0",
    "react-router-dom": "^6.18.0",
    "dayjs": "^1.11.0",
    "clsx": "^2.0.0",
    "lodash-es": "^4.17.21"
  },
  "devDependencies": {
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "@types/node": "^20.0.0",
    "typescript": "^5.0.0",
    "vite": "^5.0.0",
    "tailwindcss": "^3.3.0",
    "postcss": "^8.4.0",
    "autoprefixer": "^10.4.0"
  }
}
```

---

### 步骤4: 创建项目结构（1小时）

```bash
# 创建目录结构
mkdir -p src/{components,pages,hooks,stores,services,utils,styles,types}

# 创建文件
touch src/components/.gitkeep
touch src/pages/.gitkeep
touch src/hooks/.gitkeep
touch src/stores/.gitkeep
touch src/services/.gitkeep
touch src/utils/.gitkeep
touch src/types/.gitkeep
```

**项目结构**:
```
src/
├── components/          # React组件
│   ├── common/         # 通用组件
│   ├── layout/         # 布局组件
│   └── forms/          # 表单组件
├── pages/              # 页面
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   ├── Templates.tsx
│   ├── Upload.tsx
│   └── Results.tsx
├── hooks/              # 自定义Hook
│   ├── useAuth.ts
│   ├── useAnalysis.ts
│   └── useFetch.ts
├── stores/             # Zustand状态
│   ├── authStore.ts
│   ├── analysisStore.ts
│   └── uiStore.ts
├── services/           # API调用
│   ├── api.ts
│   ├── authService.ts
│   ├── analysisService.ts
│   └── templateService.ts
├── utils/              # 工具函数
│   ├── constants.ts
│   ├── validators.ts
│   └── formatters.ts
├── types/              # TypeScript类型
│   ├── index.ts
│   ├── auth.ts
│   ├── analysis.ts
│   └── api.ts
├── styles/             # 样式文件
│   └── index.css
├── App.tsx
├── main.tsx
└── vite-env.d.ts
```

---

### 步骤5: 创建基础文件（2小时）

#### 创建 src/types/index.ts

```typescript
// 用户类型
export interface User {
  user_id: string;
  email: string;
  name: string;
  avatar?: string;
  subscription_plan: 'free' | 'basic' | 'premium';
}

// 认证类型
export interface AuthResponse {
  access_token: string;
  user: User;
}

// 分析类型
export interface Analysis {
  analysis_id: string;
  analysis_name: string;
  status: 'processing' | 'completed' | 'failed';
  created_at: string;
  updated_at: string;
}

// 模板类型
export interface Template {
  template_id: string;
  name: string;
  category: string;
  description: string;
  metrics: Metric[];
}

export interface Metric {
  name: string;
  formula: string;
  type: string;
}

// API响应类型
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}
```

#### 创建 src/services/api.ts

```typescript
import axios, { AxiosInstance, AxiosError } from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8000/v1';

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token');
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
api.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // 清除token，重定向到登录
      localStorage.removeItem('access_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

#### 创建 src/stores/authStore.ts

```typescript
import { create } from 'zustand';
import { User } from '../types';

interface AuthStore {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  error: string | null;
  setUser: (user: User | null) => void;
  setToken: (token: string | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  token: localStorage.getItem('access_token'),
  isLoading: false,
  error: null,
  setUser: (user) => set({ user }),
  setToken: (token) => {
    if (token) {
      localStorage.setItem('access_token', token);
    } else {
      localStorage.removeItem('access_token');
    }
    set({ token });
  },
  setLoading: (loading) => set({ isLoading: loading }),
  setError: (error) => set({ error }),
  logout: () => {
    localStorage.removeItem('access_token');
    set({ user: null, token: null });
  },
}));
```

#### 创建 src/App.tsx

```typescript
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import './styles/index.css';

function App() {
  const token = useAuthStore((state) => state.token);

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/dashboard"
          element={token ? <Dashboard /> : <Navigate to="/login" />}
        />
        <Route path="/" element={<Navigate to="/dashboard" />} />
      </Routes>
    </Router>
  );
}

export default App;
```

---

### 步骤6: 创建基础组件（1小时）

#### 创建 src/components/common/Button.tsx

```typescript
import clsx from 'clsx';

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  loading?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  loading = false,
  className,
  children,
  ...props
}) => {
  const baseStyles = 'font-medium rounded-lg transition duration-200 flex items-center justify-center';
  
  const variantStyles = {
    primary: 'bg-primary-500 text-white hover:bg-primary-600 disabled:bg-gray-300',
    secondary: 'bg-gray-100 text-gray-900 hover:bg-gray-200 disabled:bg-gray-100',
    danger: 'bg-red-500 text-white hover:bg-red-600 disabled:bg-gray-300',
  };

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  return (
    <button
      className={clsx(baseStyles, variantStyles[variant], sizeStyles[size], className)}
      disabled={loading || props.disabled}
      {...props}
    >
      {loading && <span className="mr-2">⏳</span>}
      {children}
    </button>
  );
};
```

#### 创建 src/components/common/Input.tsx

```typescript
import clsx from 'clsx';

interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input: React.FC<InputProps> = ({
  label,
  error,
  className,
  ...props
}) => {
  return (
    <div className="w-full">
      {label && (
        <label className="block text-sm font-medium text-gray-700 mb-1">
          {label}
        </label>
      )}
      <input
        className={clsx(
          'w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-primary-500',
          error ? 'border-red-500' : 'border-gray-200',
          className
        )}
        {...props}
      />
      {error && <p className="text-red-500 text-sm mt-1">{error}</p>}
    </div>
  );
};
```

---

### 步骤7: 启动开发服务器（15分钟）

```bash
# 启动开发服务器
npm run dev

# 输出应该显示:
# ➜  Local:   http://localhost:5173/
# ➜  press h to show help
```

访问 http://localhost:5173 查看项目

---

## ✅ 验证清单

- [ ] 项目创建成功
- [ ] 依赖安装完成
- [ ] TailwindCSS配置完成
- [ ] 项目结构创建完成
- [ ] 基础文件创建完成
- [ ] 开发服务器可启动
- [ ] 浏览器可访问 http://localhost:5173

---

## 📊 项目统计

| 项目 | 数值 |
|------|------|
| React版本 | 18.2+ |
| TypeScript | 5.0+ |
| 依赖数量 | 10+ |
| 开发依赖 | 5+ |
| 组件数量 | 2+ |
| 文件数量 | 10+ |

---

## 🚀 下一步

1. **创建页面** - 实现Login、Dashboard等页面
2. **创建API服务** - 实现authService、analysisService等
3. **创建Hook** - 实现useAuth、useAnalysis等
4. **集成API** - 连接后端API
5. **测试** - 编写单元测试和集成测试

---

## 📞 常见问题

**Q: 如何修改API地址？**  
A: 修改 `src/services/api.ts` 中的 `API_BASE_URL`

**Q: 如何添加新页面？**  
A: 在 `src/pages/` 创建新文件，然后在 `App.tsx` 中添加路由

**Q: 如何使用TailwindCSS？**  
A: 直接在className中使用Tailwind类名，如 `className="bg-primary-500 text-white"`

---

**准备好开始前端开发了吗？** 🚀

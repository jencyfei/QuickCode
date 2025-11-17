# 开发规范指南

## 📋 目录
1. [代码风格](#代码风格)
2. [文件结构](#文件结构)
3. [命名规范](#命名规范)
4. [组件开发](#组件开发)
5. [状态管理](#状态管理)
6. [API集成](#api集成)
7. [测试规范](#测试规范)
8. [Git工作流](#git工作流)

---

## 代码风格

### TypeScript
```typescript
// ✅ 好的做法
interface UserProps {
  id: string;
  name: string;
  email: string;
}

const User: React.FC<UserProps> = ({ id, name, email }) => {
  return <div>{name}</div>;
};

// ❌ 避免
const User = (props: any) => {
  return <div>{props.name}</div>;
};
```

### 命名约定
- **组件**: PascalCase (e.g., `LoginForm.tsx`)
- **函数**: camelCase (e.g., `getUserData()`)
- **常量**: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)
- **类型**: PascalCase (e.g., `UserData`)

### 代码格式
- 使用 Prettier 自动格式化
- 行长限制: 100字符
- 缩进: 2空格
- 分号: 必需
- 单引号: 使用

```bash
npm run format
```

---

## 文件结构

### 推荐的项目结构
```
src/
├── components/
│   ├── Common/           # 通用组件
│   │   ├── Button.tsx
│   │   ├── Input.tsx
│   │   └── Loading.tsx
│   ├── Auth/             # 认证相关
│   │   ├── LoginForm.tsx
│   │   └── ProtectedRoute.tsx
│   ├── Dashboard/        # Dashboard相关
│   │   ├── StatCard.tsx
│   │   └── ChartCard.tsx
│   └── Layout/           # 布局组件
│       ├── Header.tsx
│       ├── Sidebar.tsx
│       └── Footer.tsx
├── pages/
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── TemplatesPage.tsx
│   ├── UploadPage.tsx
│   ├── ResultsPage.tsx
│   └── NotFoundPage.tsx
├── services/
│   ├── api.ts            # API客户端
│   ├── auth.ts           # 认证服务
│   ├── template.ts       # 模板服务
│   ├── upload.ts         # 上传服务
│   └── analysis.ts       # 分析服务
├── hooks/
│   ├── useAuth.ts
│   ├── useFetch.ts
│   ├── useForm.ts
│   └── useLocalStorage.ts
├── store/
│   ├── authSlice.ts      # Redux切片
│   ├── templateSlice.ts
│   └── index.ts
├── types/
│   └── index.ts          # 类型定义
├── utils/
│   ├── format.ts         # 格式化工具
│   ├── validate.ts       # 验证工具
│   └── constants.ts      # 常量
├── styles/
│   └── globals.css       # 全局样式
├── App.tsx
└── main.tsx
```

---

## 命名规范

### 文件命名
```
组件文件:     Button.tsx, LoginForm.tsx
页面文件:     LoginPage.tsx, DashboardPage.tsx
服务文件:     auth.ts, api.ts
Hook文件:     useAuth.ts, useFetch.ts
类型文件:     types.ts, index.ts
样式文件:     globals.css, Button.module.css
```

### 变量命名
```typescript
// 布尔值: is/has/can前缀
const isLoading = true;
const hasError = false;
const canSubmit = true;

// 数组: 复数形式
const users: User[] = [];
const items: Item[] = [];

// 事件处理: on前缀
const handleClick = () => {};
const onSubmit = () => {};
const onChangeInput = () => {};

// 回调函数: on前缀
const onSuccess = () => {};
const onError = () => {};
```

---

## 组件开发

### 函数组件模板
```typescript
import { FC, ReactNode } from 'react';
import clsx from 'clsx';

interface ComponentProps {
  children?: ReactNode;
  className?: string;
  // 其他props
}

const Component: FC<ComponentProps> = ({
  children,
  className,
  ...props
}) => {
  return (
    <div className={clsx('base-class', className)}>
      {children}
    </div>
  );
};

export default Component;
```

### 组件最佳实践
1. **单一职责**: 每个组件只做一件事
2. **可复用性**: 设计通用组件
3. **类型安全**: 完整的TypeScript类型
4. **文档注释**: 添加JSDoc注释
5. **错误处理**: 处理边界情况

```typescript
/**
 * 用户卡片组件
 * @param user - 用户信息
 * @param onDelete - 删除回调
 */
interface UserCardProps {
  user: User;
  onDelete?: (id: string) => void;
}

const UserCard: FC<UserCardProps> = ({ user, onDelete }) => {
  return (
    <div className="glass-card p-4">
      <h3>{user.name}</h3>
      {onDelete && (
        <button onClick={() => onDelete(user.id)}>删除</button>
      )}
    </div>
  );
};
```

---

## 状态管理

### Redux Toolkit (全局状态)
```typescript
// store/authSlice.ts
import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import { User } from '@/types';

interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
}

const initialState: AuthState = {
  user: null,
  token: null,
  isLoading: false,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    setUser: (state, action: PayloadAction<User>) => {
      state.user = action.payload;
    },
    setToken: (state, action: PayloadAction<string>) => {
      state.token = action.payload;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
    },
  },
});

export const { setUser, setToken, logout } = authSlice.actions;
export default authSlice.reducer;
```

### React Hooks (本地状态)
```typescript
// 使用useState处理本地状态
const [formData, setFormData] = useState({
  email: '',
  password: '',
});

// 使用useEffect处理副作用
useEffect(() => {
  // 初始化或清理
  return () => {
    // 清理函数
  };
}, [dependencies]);
```

---

## API集成

### 服务层模式
```typescript
// services/user.ts
import apiClient from './api';
import { User, ApiResponse } from '@/types';

export const userService = {
  // 获取用户列表
  getUsers: (page: number = 1): Promise<ApiResponse<User[]>> =>
    apiClient.get(`/users?page=${page}`),

  // 获取用户详情
  getUserById: (id: string): Promise<ApiResponse<User>> =>
    apiClient.get(`/users/${id}`),

  // 创建用户
  createUser: (data: Partial<User>): Promise<ApiResponse<User>> =>
    apiClient.post('/users', data),

  // 更新用户
  updateUser: (id: string, data: Partial<User>): Promise<ApiResponse<User>> =>
    apiClient.put(`/users/${id}`, data),

  // 删除用户
  deleteUser: (id: string): Promise<ApiResponse<void>> =>
    apiClient.delete(`/users/${id}`),
};
```

### 在组件中使用
```typescript
const UserList = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUsers = async () => {
      setIsLoading(true);
      try {
        const response = await userService.getUsers();
        setUsers(response.data);
      } catch (err) {
        setError(err instanceof Error ? err.message : '获取失败');
      } finally {
        setIsLoading(false);
      }
    };

    fetchUsers();
  }, []);

  if (isLoading) return <Loading />;
  if (error) return <div>错误: {error}</div>;

  return (
    <div>
      {users.map(user => (
        <UserCard key={user.id} user={user} />
      ))}
    </div>
  );
};
```

---

## 测试规范

### 单元测试
```typescript
// Button.test.tsx
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Button from './Button';

describe('Button Component', () => {
  it('renders button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick handler when clicked', async () => {
    const handleClick = vi.fn();
    render(<Button onClick={handleClick}>Click</Button>);
    
    await userEvent.click(screen.getByText('Click'));
    expect(handleClick).toHaveBeenCalledOnce();
  });

  it('disables button when disabled prop is true', () => {
    render(<Button disabled>Click</Button>);
    expect(screen.getByText('Click')).toBeDisabled();
  });
});
```

### 测试命令
```bash
npm run test              # 运行测试
npm run test:ui          # UI模式
npm run test:coverage    # 覆盖率报告
```

---

## Git工作流

### 分支命名
```
feature/功能名称        # 新功能
bugfix/bug名称         # 修复bug
hotfix/问题名称        # 紧急修复
refactor/重构名称      # 代码重构
docs/文档名称          # 文档更新
```

### 提交信息格式
```
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档
- `style`: 代码风格
- `refactor`: 代码重构
- `perf`: 性能优化
- `test`: 测试
- `chore`: 构建/工具

### 示例
```
feat(auth): 添加登录表单验证

- 添加邮箱格式验证
- 添加密码强度检查
- 添加错误提示

Closes #123
```

---

## 代码审查检查清单

- [ ] 代码遵循命名规范
- [ ] 有完整的TypeScript类型
- [ ] 有JSDoc注释
- [ ] 没有console.log
- [ ] 没有硬编码的值
- [ ] 有错误处理
- [ ] 有单元测试
- [ ] 代码已格式化
- [ ] 没有未使用的变量
- [ ] 提交信息清晰

---

## 常见错误

### ❌ 避免
```typescript
// 1. 使用any类型
const data: any = response.data;

// 2. 直接修改状态
state.user.name = 'New Name';

// 3. 在循环中使用索引作为key
{items.map((item, index) => <div key={index}>{item}</div>)}

// 4. 在render中创建函数
<button onClick={() => handleClick()}>Click</button>

// 5. 忘记依赖数组
useEffect(() => {
  // 代码
}); // ❌ 没有依赖数组
```

### ✅ 推荐
```typescript
// 1. 使用具体类型
const data: User = response.data;

// 2. 使用不可变更新
setUser({ ...user, name: 'New Name' });

// 3. 使用唯一ID作为key
{items.map(item => <div key={item.id}>{item}</div>)}

// 4. 提前定义函数
const handleClick = () => { /* ... */ };
<button onClick={handleClick}>Click</button>

// 5. 添加依赖数组
useEffect(() => {
  // 代码
}, [dependency]);
```

---

## 性能优化

### 代码分割
```typescript
import { lazy, Suspense } from 'react';
import Loading from '@/components/Common/Loading';

const DashboardPage = lazy(() => import('@/pages/DashboardPage'));

export const routes = [
  {
    path: '/dashboard',
    element: (
      <Suspense fallback={<Loading fullScreen />}>
        <DashboardPage />
      </Suspense>
    ),
  },
];
```

### 记忆化
```typescript
import { memo, useMemo, useCallback } from 'react';

// 记忆化组件
const UserCard = memo(({ user }: { user: User }) => {
  return <div>{user.name}</div>;
});

// 记忆化值
const expensiveValue = useMemo(() => {
  return computeExpensiveValue(data);
}, [data]);

// 记忆化函数
const handleClick = useCallback(() => {
  // 处理点击
}, [dependencies]);
```

---

## 调试技巧

### React DevTools
- 安装 React DevTools 浏览器扩展
- 检查组件树和props
- 分析性能

### 浏览器控制台
```typescript
// 添加调试日志
console.log('数据:', data);
console.warn('警告:', warning);
console.error('错误:', error);

// 条件日志
if (import.meta.env.DEV) {
  console.log('开发环境日志');
}
```

---

## 资源链接

- [React官方文档](https://react.dev)
- [TypeScript官方文档](https://www.typescriptlang.org)
- [TailwindCSS官方文档](https://tailwindcss.com)
- [Redux官方文档](https://redux.js.org)
- [Vite官方文档](https://vitejs.dev)

---

**最后更新**: 2025年11月17日  
**版本**: 1.0.0

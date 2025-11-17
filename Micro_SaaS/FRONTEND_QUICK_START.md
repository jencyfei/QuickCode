# 🚀 DataViz Insights - 前端快速开始

**最后更新**: 2025年11月17日  
**状态**: ✅ 项目配置完成

---

## 📋 前置要求

### 系统要求
- Node.js >= 18.0.0
- npm >= 9.0.0 (或 pnpm >= 8.0.0)
- Git

### 开发工具
- VS Code (推荐)
- VS Code Extensions:
  - ES7+ React/Redux/React-Native snippets
  - Prettier - Code formatter
  - ESLint
  - Tailwind CSS IntelliSense

---

## 🎯 项目初始化

### 第1步：克隆项目
```bash
git clone <repository-url>
cd dataviz-insights
```

### 第2步：安装依赖
```bash
# 使用npm
npm install

# 或使用pnpm (推荐，更快)
pnpm install
```

### 第3步：配置环境变量
```bash
# 复制环境变量示例文件
cp .env.example .env.local

# 编辑.env.local，配置API地址
# VITE_API_BASE_URL=http://localhost:8000/v1
```

### 第4步：启动开发服务器
```bash
npm run dev
# 或
pnpm dev
```

浏览器会自动打开 http://localhost:5173

---

## 📂 项目结构

```
dataviz-insights/
├── public/                 # 静态资源
├── src/
│   ├── components/        # React组件
│   │   ├── Layout/       # 布局组件
│   │   ├── Auth/         # 认证组件
│   │   ├── Dashboard/    # Dashboard组件
│   │   ├── Templates/    # 模板组件
│   │   ├── Upload/       # 上传组件
│   │   ├── Results/      # 结果组件
│   │   └── Common/       # 通用组件
│   ├── pages/            # 页面组件
│   ├── services/         # API服务
│   ├── store/            # Redux状态管理
│   ├── hooks/            # 自定义Hooks
│   ├── utils/            # 工具函数
│   ├── types/            # TypeScript类型定义
│   ├── styles/           # 全局样式
│   ├── App.tsx           # 主应用组件
│   └── main.tsx          # 入口文件
├── package.json          # 项目配置
├── tsconfig.json         # TypeScript配置
├── vite.config.ts        # Vite配置
├── tailwind.config.js    # TailwindCSS配置
├── postcss.config.js     # PostCSS配置
└── .env.example          # 环境变量示例
```

---

## 🔧 常用命令

### 开发命令
```bash
# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 运行测试
npm run test

# 运行测试并显示UI
npm run test:ui

# 生成测试覆盖率报告
npm run test:coverage
```

### 代码质量命令
```bash
# 检查代码质量
npm run lint

# 格式化代码
npm run format

# 类型检查
npm run type-check
```

---

## 🎨 设计系统

### 色彩系统
```css
/* 品牌色 */
--primary: #6366F1;        /* Indigo 600 */
--secondary: #EC4899;      /* Pink 500 */

/* 背景色 */
--bg-light: #F0F4F8;       /* 浅蓝灰 */
--bg-medium: #E8EEF5;      /* 中蓝灰 */
--bg-dark: #DFE9F3;        /* 深蓝灰 */

/* 文字色 */
--text-primary: #333333;   /* 深灰 */
--text-secondary: #4A4A4A; /* 中灰 */
--text-tertiary: #8A8A8A;  /* 浅灰 */
```

### 排版规范
```
字体族: Inter, SF Pro, -apple-system
标题: 32px / 600 weight
副标题: 24px / 600 weight
正文: 16px / 400 weight
标签: 14px / 500 weight
```

### 组件示例
```tsx
// Button组件
<button className="px-4 py-2 bg-primary text-white rounded-lg hover:opacity-90">
  Click me
</button>

// Card组件
<div className="bg-glass-light backdrop-blur-lg border border-glass-border rounded-2xl p-6">
  Content
</div>

// Input组件
<input 
  className="w-full px-4 py-2 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-primary"
  placeholder="Enter text"
/>
```

---

## 🔌 API集成

### API基础配置
```typescript
// src/services/api.ts
import axios from 'axios';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: import.meta.env.VITE_API_TIMEOUT,
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 处理未授权
    }
    return Promise.reject(error);
  }
);

export default apiClient;
```

### API调用示例
```typescript
// src/services/auth.ts
import apiClient from './api';

export const authService = {
  login: (email: string, password: string) =>
    apiClient.post('/auth/login', { email, password }),
  
  register: (data: RegisterData) =>
    apiClient.post('/auth/register', data),
  
  logout: () =>
    apiClient.post('/auth/logout'),
};
```

---

## 🧪 测试

### 单元测试示例
```typescript
// src/components/Button.test.tsx
import { render, screen } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders button with text', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick handler', () => {
    const onClick = vi.fn();
    render(<Button onClick={onClick}>Click</Button>);
    screen.getByText('Click').click();
    expect(onClick).toHaveBeenCalled();
  });
});
```

### 运行测试
```bash
# 运行所有测试
npm run test

# 运行特定文件的测试
npm run test Button.test.tsx

# 监听模式
npm run test -- --watch

# 生成覆盖率报告
npm run test:coverage
```

---

## 🚀 部署

### 构建生产版本
```bash
npm run build
```

生成的文件在 `dist/` 目录中。

### 部署到Vercel
```bash
# 安装Vercel CLI
npm i -g vercel

# 部署
vercel
```

### 部署到Netlify
```bash
# 安装Netlify CLI
npm i -g netlify-cli

# 部署
netlify deploy --prod --dir=dist
```

---

## 🐛 调试

### 浏览器开发者工具
- F12 打开开发者工具
- Console 查看日志
- Network 查看网络请求
- Performance 分析性能

### VS Code调试
```json
// .vscode/launch.json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Launch Chrome",
      "url": "http://localhost:5173",
      "webRoot": "${workspaceFolder}/src",
      "sourceMapPathOverride": {
        "webpack:///src/*": "${webspaceFolder}/src/*"
      }
    }
  ]
}
```

---

## 📚 学习资源

### 官方文档
- [React官方文档](https://react.dev)
- [TypeScript官方文档](https://www.typescriptlang.org)
- [TailwindCSS官方文档](https://tailwindcss.com)
- [Vite官方文档](https://vitejs.dev)

### 推荐教程
- [React Hooks深入](https://react.dev/reference/react)
- [TypeScript最佳实践](https://www.typescriptlang.org/docs/handbook/)
- [TailwindCSS实战](https://tailwindcss.com/docs)

### 工具和库
- [Redux Toolkit](https://redux-toolkit.js.org)
- [Axios](https://axios-http.com)
- [Recharts](https://recharts.org)
- [Chart.js](https://www.chartjs.org)

---

## ❓ 常见问题

### Q: 如何添加新的环境变量？
A: 在`.env.local`中添加，然后在代码中使用`import.meta.env.VITE_*`访问。

### Q: 如何修改TailwindCSS配置？
A: 编辑`tailwind.config.js`文件，修改后会自动重新加载。

### Q: 如何调试API请求？
A: 使用浏览器开发者工具的Network标签，或在`src/services/api.ts`中添加日志。

### Q: 如何优化构建大小？
A: 使用`npm run build`生成报告，然后分析哪些包可以优化。

### Q: 如何处理CORS问题？
A: 在`vite.config.ts`中配置代理，或在后端配置CORS头。

---

## 📞 获取帮助

### 遇到问题？
1. 检查错误信息
2. 查看浏览器控制台
3. 查看网络请求
4. 查看项目文档
5. 提交Issue

### 提交Issue
```
标题: [BUG] 简短描述
内容:
- 问题描述
- 复现步骤
- 期望结果
- 实际结果
- 环境信息
```

---

## ✅ 检查清单

在开始开发前，请确保：

- [ ] Node.js >= 18.0.0
- [ ] npm >= 9.0.0
- [ ] 项目已克隆
- [ ] 依赖已安装
- [ ] 环境变量已配置
- [ ] 开发服务器可以启动
- [ ] 浏览器可以访问应用

---

## 🎉 开始开发

现在你已经准备好开始开发了！

```bash
# 启动开发服务器
npm run dev

# 打开浏览器访问
http://localhost:5173
```

**祝你开发愉快！** 🚀✨

---

**更新时间**: 2025年11月17日  
**维护人**: Cascade  
**状态**: ✅ 完成

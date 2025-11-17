# 故障排除指南

## 🔧 常见问题和解决方案

### 1. npm install 失败

#### 问题
```
npm ERR! code ERESOLVE
npm ERR! ERESOLVE unable to resolve dependency tree
```

#### 解决方案
```bash
# 方案1: 使用 --legacy-peer-deps
npm install --legacy-peer-deps

# 方案2: 清除缓存后重试
npm cache clean --force
npm install

# 方案3: 使用 pnpm (推荐)
pnpm install
```

---

### 2. 开发服务器无法启动

#### 问题
```
Error: listen EADDRINUSE: address already in use :::5173
```

#### 解决方案
```bash
# 方案1: 使用不同的端口
npm run dev -- --port 5174

# 方案2: 杀死占用端口的进程
# Windows
netstat -ano | findstr :5173
taskkill /PID <PID> /F

# macOS/Linux
lsof -i :5173
kill -9 <PID>
```

---

### 3. TypeScript 编译错误

#### 问题
```
Cannot find module 'react' or its corresponding type declarations
```

#### 解决方案
```bash
# 重新安装依赖
rm -rf node_modules package-lock.json
npm install

# 或清除TypeScript缓存
rm -rf dist
npm run build
```

---

### 4. 样式不生效

#### 问题
TailwindCSS 类名不生效

#### 解决方案
```bash
# 1. 检查 tailwind.config.js 中的 content 配置
# 确保包含所有模板文件路径

# 2. 重启开发服务器
npm run dev

# 3. 清除缓存
rm -rf .next dist node_modules/.cache
npm run dev
```

---

### 5. API 请求失败

#### 问题
```
Error: Network Error
CORS error
```

#### 解决方案

**CORS错误**:
```typescript
// 检查 vite.config.ts 中的代理配置
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8000',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/v1'),
    },
  },
}
```

**网络错误**:
```bash
# 1. 检查API服务器是否运行
curl http://localhost:8000/v1/health

# 2. 检查环境变量
cat .env.local

# 3. 检查浏览器控制台的Network标签
```

---

### 6. 路由无法工作

#### 问题
页面刷新后404错误

#### 解决方案
```typescript
// vite.config.ts 中添加
server: {
  middlewareMode: true,
}

// 或使用 history 模式
import { createHashHistory } from 'history';
```

---

### 7. 状态管理问题

#### 问题
Redux状态未更新

#### 解决方案
```typescript
// ✅ 正确: 使用不可变更新
dispatch(setUser({ ...user, name: 'New' }));

// ❌ 错误: 直接修改状态
user.name = 'New';
dispatch(setUser(user));

// 使用Redux DevTools调试
// 1. 安装 Redux DevTools 浏览器扩展
// 2. 在浏览器中打开 Redux 标签
// 3. 查看状态变化
```

---

### 8. 性能问题

#### 问题
应用加载缓慢

#### 解决方案
```bash
# 1. 分析构建大小
npm run build -- --report

# 2. 启用代码分割
# 在 vite.config.ts 中配置

# 3. 使用性能分析工具
# React DevTools Profiler

# 4. 检查网络请求
# 浏览器 DevTools -> Network 标签
```

---

### 9. 内存泄漏

#### 问题
应用运行一段时间后变慢

#### 解决方案
```typescript
// ✅ 正确: 清理副作用
useEffect(() => {
  const timer = setInterval(() => {
    // 代码
  }, 1000);

  return () => clearInterval(timer); // 清理
}, []);

// ❌ 错误: 未清理
useEffect(() => {
  const timer = setInterval(() => {
    // 代码
  }, 1000);
}, []);
```

---

### 10. 浏览器兼容性问题

#### 问题
在某些浏览器中无法工作

#### 解决方案
```bash
# 1. 检查浏览器支持
# 使用 caniuse.com

# 2. 添加 polyfill
npm install core-js

# 3. 配置 Vite 的 target
// vite.config.ts
build: {
  target: 'es2020',
}
```

---

## 🐛 调试技巧

### 1. 使用 console 调试
```typescript
// 基本日志
console.log('值:', value);

// 分组日志
console.group('用户信息');
console.log('名称:', user.name);
console.log('邮箱:', user.email);
console.groupEnd();

// 表格显示
console.table(users);

// 性能测试
console.time('操作');
// 代码
console.timeEnd('操作');
```

### 2. 使用 debugger
```typescript
const handleClick = () => {
  debugger; // 在这里暂停执行
  // 代码
};
```

### 3. 使用 React DevTools
- 检查组件树
- 查看 props 和 state
- 分析性能
- 追踪组件更新

### 4. 使用 Redux DevTools
- 查看状态树
- 追踪 action
- 时间旅行调试
- 导出/导入状态

---

## 📋 检查清单

遇到问题时，按以下步骤检查：

- [ ] 错误信息是什么？
- [ ] 在浏览器控制台中查看完整错误
- [ ] 检查网络请求 (Network 标签)
- [ ] 检查应用状态 (Redux DevTools)
- [ ] 检查组件树 (React DevTools)
- [ ] 查看相关文档
- [ ] 搜索 Stack Overflow
- [ ] 提交 Issue

---

## 🆘 获取帮助

### 提交 Issue 时包含
1. 完整的错误信息
2. 重现步骤
3. 期望行为
4. 实际行为
5. 环境信息 (Node版本、npm版本等)

### 示例
```markdown
## 问题描述
登录后无法跳转到 Dashboard

## 重现步骤
1. 打开登录页面
2. 输入邮箱和密码
3. 点击登录按钮

## 期望行为
应该跳转到 Dashboard 页面

## 实际行为
页面停留在登录页面，控制台显示错误

## 错误信息
```
Error: Cannot read property 'id' of undefined
```

## 环境
- Node: 18.0.0
- npm: 9.0.0
- OS: Windows 10
```

---

## 📚 有用的资源

### 官方文档
- [React 文档](https://react.dev)
- [TypeScript 文档](https://www.typescriptlang.org/docs)
- [Vite 文档](https://vitejs.dev)
- [TailwindCSS 文档](https://tailwindcss.com/docs)

### 社区资源
- [Stack Overflow](https://stackoverflow.com)
- [GitHub Discussions](https://github.com)
- [Dev.to](https://dev.to)

### 工具
- [Chrome DevTools](https://developer.chrome.com/docs/devtools)
- [React DevTools](https://react-devtools-tutorial.vercel.app)
- [Redux DevTools](https://github.com/reduxjs/redux-devtools)

---

**最后更新**: 2025年11月17日  
**版本**: 1.0.0

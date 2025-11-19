# 🚀 开发环境启动指南

**最后更新**: 2025-11-19  
**状态**: ✅ 开发服务器已启动

---

## ✅ 已完成的工作

### 1. 依赖安装
```bash
npm install --legacy-peer-deps
# 已安装 551 个包
# 4 个中等严重性漏洞（不影响开发）
```

### 2. 配置修复
- ✅ 创建 `tsconfig.node.json` - Node.js TypeScript 配置
- ✅ 重命名 `postcss.config.js` → `postcss.config.cjs` - CommonJS 格式
- ✅ 重命名 `tailwind.config.js` → `tailwind.config.cjs` - CommonJS 格式
- ✅ 更新配置文件为 CommonJS 格式

### 3. 开发服务器启动
```bash
npm run dev
# 服务器运行在 http://localhost:5173
# 支持热模块替换 (HMR)
```

---

## 🌐 访问应用

### 本地开发
- **主页**: http://localhost:5173
- **登录页**: http://localhost:5173/login
- **注册页**: http://localhost:5173/register
- **仪表板**: http://localhost:5173/dashboard (需要登录)
- **模板页**: http://localhost:5173/templates
- **上传页**: http://localhost:5173/upload
- **结果页**: http://localhost:5173/results

### 浏览器预览
- 已配置浏览器预览代理
- 支持实时热更新
- 自动刷新页面

---

## 📦 项目结构

```
src/
├── components/
│   ├── Common/          # 通用组件 (Button, Input, Card, Modal, Loading)
│   └── Layout/          # 布局组件 (Navbar, Sidebar, Layout)
├── pages/               # 页面组件 (7个页面)
├── services/            # 服务层 (API, Auth)
├── hooks/               # 自定义 Hook (useAuth)
├── types/               # TypeScript 类型定义
├── styles/              # 全局样式
├── App.tsx              # 主应用组件
└── main.tsx             # 入口文件
```

---

## 🛠️ 常用命令

### 开发
```bash
# 启动开发服务器
npm run dev

# 代码检查
npm run lint

# 代码格式化
npm run format

# 类型检查
npm run type-check
```

### 构建
```bash
# 生产构建
npm run build

# 预览构建结果
npm run preview
```

### 测试
```bash
# 运行测试
npm run test

# 测试覆盖率
npm run test:coverage
```

---

## 🎨 设计系统

### 色彩系统
```
主色: #6366F1 (Indigo)
辅色: #8B5CF6 (Violet)
背景: linear-gradient(135deg, #F0F4F8 0%, #E8EEF5 50%, #DFE9F3 100%)
文字: #333333 (深灰)
```

### 玻璃拟态风格
- 半透明背景 (rgba)
- 模糊效果 (backdrop-filter)
- 柔和阴影
- 圆角设计 (12-24px)

### 响应式断点
- 移动端: 375px
- 平板: 768px
- 桌面: 1024px+

---

## 🔧 配置文件说明

### vite.config.ts
- 构建工具配置
- 路径别名配置
- 开发服务器配置
- API 代理配置

### tsconfig.json
- TypeScript 编译选项
- 路径映射
- 严格模式启用

### tsconfig.node.json
- Node.js 相关的 TypeScript 配置
- Vite 配置文件的类型检查

### tailwind.config.cjs
- TailwindCSS 配置
- 自定义颜色、间距、动画
- 玻璃拟态效果定义

### postcss.config.cjs
- PostCSS 插件配置
- 目前禁用所有插件（避免依赖问题）

---

## 📝 开发工作流

### 1. 创建新组件
```typescript
// src/components/Common/NewComponent.tsx
import React from 'react'

interface NewComponentProps {
  // 定义 props
}

export const NewComponent: React.FC<NewComponentProps> = (props) => {
  return (
    <div className="...">
      {/* 组件内容 */}
    </div>
  )
}
```

### 2. 创建新页面
```typescript
// src/pages/NewPage.tsx
import React from 'react'
import { Layout } from '@components/Layout/Layout'

export const NewPage: React.FC = () => {
  return (
    <Layout>
      {/* 页面内容 */}
    </Layout>
  )
}
```

### 3. 添加路由
```typescript
// src/App.tsx
import { NewPage } from '@pages/NewPage'

// 在路由配置中添加
<Route path="/new-page" element={<NewPage />} />
```

---

## 🐛 常见问题

### Q: 开发服务器无法启动
**A**: 
1. 确保已安装依赖: `npm install --legacy-peer-deps`
2. 检查端口 5173 是否被占用
3. 清除 node_modules: `rm -r node_modules && npm install`

### Q: 样式不生效
**A**:
1. 确保导入了全局样式: `import '@styles/globals.css'`
2. 检查 TailwindCSS 配置
3. 清除浏览器缓存

### Q: TypeScript 错误
**A**:
1. 运行 `npm run type-check` 检查类型
2. 确保所有导入路径正确
3. 检查 tsconfig.json 配置

### Q: 热更新不工作
**A**:
1. 检查浏览器控制台是否有错误
2. 重启开发服务器: `npm run dev`
3. 清除浏览器缓存

---

## 📚 相关文档

| 文档 | 说明 |
|------|------|
| **PHASE1_COMPLETION_SUMMARY.md** | 第1阶段完成总结 |
| **DEVELOPMENT_PROGRESS_DETAILED.md** | 详细进度跟踪 |
| **DEVELOPMENT_ROADMAP.md** | 完整开发路线图 |
| **QUICK_REFERENCE.md** | 快速参考卡片 |

---

## 🚀 下一步

### 立即开始
1. 打开浏览器访问 http://localhost:5173
2. 查看登录页面
3. 开始开发新功能

### 第2阶段任务
1. ✅ 认证系统完善
2. ✅ API 集成
3. ✅ 表单验证
4. ✅ 错误处理

---

## 💡 开发建议

### 代码规范
- 使用 TypeScript 进行类型检查
- 遵循 ESLint 规则
- 使用 Prettier 格式化代码
- 编写清晰的注释

### 组件设计
- 单一职责原则
- 可复用性优先
- Props 类型完整定义
- 支持响应式设计

### 性能优化
- 使用 React.memo 避免不必要的重新渲染
- 使用 useMemo 和 useCallback 优化性能
- 代码分割和懒加载
- 图片优化

---

## 📞 获取帮助

### 常用资源
- [React 官方文档](https://react.dev)
- [TypeScript 官方文档](https://www.typescriptlang.org)
- [TailwindCSS 官方文档](https://tailwindcss.com)
- [Vite 官方文档](https://vitejs.dev)

### 项目文档
- 查看 `QUICK_REFERENCE.md` 获取快速参考
- 查看 `DEVELOPMENT_ROADMAP.md` 了解项目计划
- 查看 `PHASE1_COMPLETION_SUMMARY.md` 了解已完成工作

---

**开发环境已准备就绪！开始开发吧！🎉**

*最后更新: 2025-11-19 16:00*

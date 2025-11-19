# 🎯 快速参考卡片

## 🚀 快速开始 (5分钟)

```bash
# 1. 安装依赖
npm install

# 2. 启动开发服务器
npm run dev

# 3. 打开浏览器
# http://localhost:5173
```

---

## 📁 项目结构速览

```
src/
├── components/          # 通用组件 (待开发)
│   ├── Common/         # 基础组件
│   ├── Layout/         # 布局组件
│   └── Auth/           # 认证组件
├── pages/              # 页面组件 (待开发)
├── services/           # API 服务
├── hooks/              # 自定义 Hook
├── types/              # 类型定义
├── styles/             # 全局样式
├── App.tsx             # 主应用
└── main.tsx            # 入口
```

---

## 🎨 设计风格

**玻璃拟态 + 蓝灰中性配色**

```css
主色: #6366F1
辅色: #8B5CF6
背景: linear-gradient(135deg, #F0F4F8 0%, #E8EEF5 50%, #DFE9F3 100%)
文字: #333333
```

**参考文件**:
- `dataviz_login.html`
- `dataviz_dashboard.html`
- `dataviz_templates.html`
- `dataviz_upload.html`
- `dataviz_results.html`

---

## 📋 第1阶段任务 (优先级)

### 优先级 1: 基础组件
- [ ] Button.tsx (2-3h)
- [ ] Input.tsx (2-3h)
- [ ] Card.tsx (2-3h)
- [ ] Loading.tsx (2-3h)

### 优先级 2: 页面框架
- [ ] LoginPage.tsx (4-5h)
- [ ] DashboardPage.tsx (4-5h)
- [ ] Navbar.tsx (3-4h)
- [ ] Sidebar.tsx (3-4h)

### 优先级 3: 高级组件
- [ ] Modal.tsx (2-3h)
- [ ] TemplatesPage.tsx (4-5h)
- [ ] UploadPage.tsx (5-6h)
- [ ] ResultsPage.tsx (5-6h)

### 优先级 4: 样式优化
- [ ] 全局样式 (2-3h)
- [ ] 响应式设计
- [ ] 动画效果

**总计**: 40-50 小时

---

## 💻 常用命令

```bash
# 开发
npm run dev              # 启动开发服务器
npm run build            # 构建生产版本
npm run preview          # 预览生产版本

# 代码质量
npm run format           # 代码格式化
npm run lint             # 代码检查
npm run type-check       # 类型检查

# 测试
npm run test             # 运行测试
npm run test:ui          # 测试 UI
npm run test:coverage    # 测试覆盖率
```

---

## 🎯 开发工作流

### 1. 创建新组件

```bash
# 创建文件
touch src/components/Common/Button.tsx

# 编写组件
# 添加 TypeScript 类型
# 实现组件逻辑
# 添加 TailwindCSS 样式
# 编写测试用例
```

### 2. 创建新页面

```bash
# 创建文件
touch src/pages/LoginPage.tsx

# 参考 HTML 原型
# 使用通用组件
# 添加业务逻辑
# 集成 API
```

### 3. 提交代码

```bash
# 格式化代码
npm run format

# 检查代码
npm run lint

# 类型检查
npm run type-check

# 提交 Git
git add .
git commit -m "feat: add Button component"
git push
```

---

## 📚 关键文档

| 文档 | 说明 |
|------|------|
| `DEVELOPMENT_ROADMAP.md` | 完整开发路线图 |
| `PHASE1_TASKS.md` | 第1阶段详细任务 |
| `PROJECT_STATUS.md` | 项目状态报告 |
| `DEVELOPMENT_GUIDELINES.md` | 开发规范 |
| `3.api_design.md` | API 设计 |

---

## 🎨 TailwindCSS 常用类

```html
<!-- 间距 -->
<div class="p-4 m-2">Padding 和 Margin</div>

<!-- 颜色 -->
<div class="bg-indigo-600 text-white">背景和文字</div>

<!-- 布局 -->
<div class="flex items-center justify-between">Flexbox</div>
<div class="grid grid-cols-3 gap-4">Grid</div>

<!-- 响应式 -->
<div class="text-sm md:text-base lg:text-lg">响应式文字</div>

<!-- 悬停效果 -->
<button class="hover:bg-indigo-700 transition">按钮</button>

<!-- 圆角 -->
<div class="rounded-lg">圆角</div>

<!-- 阴影 -->
<div class="shadow-lg">阴影</div>
```

---

## 🔧 TypeScript 类型示例

```typescript
// 组件 Props
interface ButtonProps {
  type?: 'primary' | 'secondary' | 'danger'
  size?: 'sm' | 'md' | 'lg'
  disabled?: boolean
  loading?: boolean
  onClick?: () => void
  children: React.ReactNode
}

// 页面数据
interface DashboardData {
  totalProjects: number
  totalAnalyses: number
  recentProjects: Project[]
}

// API 响应
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

---

## 🧪 测试示例

```typescript
import { render, screen } from '@testing-library/react'
import Button from './Button'

describe('Button Component', () => {
  it('renders button with text', () => {
    render(<Button>Click me</Button>)
    expect(screen.getByText('Click me')).toBeInTheDocument()
  })

  it('calls onClick when clicked', () => {
    const onClick = vi.fn()
    render(<Button onClick={onClick}>Click</Button>)
    screen.getByText('Click').click()
    expect(onClick).toHaveBeenCalled()
  })
})
```

---

## 🐛 常见问题

### Q: 如何添加新的 npm 包?
```bash
npm install package-name
npm run format  # 格式化代码
```

### Q: 如何修改 TailwindCSS 配置?
编辑 `tailwind.config.js`，然后重启开发服务器

### Q: 如何调试代码?
```bash
# 在浏览器中打开 DevTools (F12)
# 在代码中添加 debugger
# 或使用 console.log
```

### Q: 如何处理 API 错误?
```typescript
try {
  const data = await api.get('/endpoint')
} catch (error) {
  console.error('API Error:', error)
  // 显示错误提示
}
```

---

## 📞 快速链接

- React 文档: https://react.dev
- TypeScript 文档: https://www.typescriptlang.org
- TailwindCSS 文档: https://tailwindcss.com
- Vite 文档: https://vitejs.dev
- Vitest 文档: https://vitest.dev

---

## ✅ 开始前检查

- [ ] Node.js >= 18.0.0
- [ ] npm >= 9.0.0
- [ ] 依赖已安装 (`npm install`)
- [ ] 开发服务器可启动 (`npm run dev`)
- [ ] 代码编辑器已配置
- [ ] 已阅读 `DEVELOPMENT_GUIDELINES.md`

---

**准备好了? 开始第1阶段! 🚀**

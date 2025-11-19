# 📋 第1阶段: 前端框架搭建 - 详细任务清单

**阶段目标**: 建立完整的 React 前端框架，实现所有页面和通用组件  
**预计工作量**: 40-50 小时  
**预计时间**: 1-2 周  
**当前状态**: ⏳ 准备开始

---

## 🎯 任务优先级

### 优先级 1: 基础组件 (必须完成)
这些组件是其他所有页面的基础

- [ ] **Button.tsx** - 按钮组件
- [ ] **Input.tsx** - 输入框组件
- [ ] **Card.tsx** - 卡片组件
- [ ] **Loading.tsx** - 加载组件

### 优先级 2: 页面框架 (必须完成)
这些页面构成应用的核心

- [ ] **LoginPage.tsx** - 登录页面
- [ ] **DashboardPage.tsx** - 仪表板
- [ ] **Navbar.tsx** - 导航栏
- [ ] **Sidebar.tsx** - 侧边栏

### 优先级 3: 高级组件 (应该完成)
这些组件增强用户体验

- [ ] **Modal.tsx** - 模态框
- [ ] **TemplatesPage.tsx** - 模板选择
- [ ] **UploadPage.tsx** - 数据上传
- [ ] **ResultsPage.tsx** - 分析结果

### 优先级 4: 样式和优化 (可以稍后完成)
这些工作改进应用质量

- [ ] 全局样式优化
- [ ] 响应式设计完善
- [ ] 动画效果添加
- [ ] 性能优化

---

## 📝 详细任务描述

### 任务 1: Button.tsx - 按钮组件

**文件位置**: `src/components/Common/Button.tsx`

**需求**:
```typescript
// 支持的类型
type ButtonType = 'primary' | 'secondary' | 'danger' | 'ghost'

// 支持的大小
type ButtonSize = 'sm' | 'md' | 'lg'

// Props 接口
interface ButtonProps {
  type?: ButtonType
  size?: ButtonSize
  disabled?: boolean
  loading?: boolean
  fullWidth?: boolean
  onClick?: () => void
  children: React.ReactNode
}
```

**参考设计**: `dataviz_login.html` 中的按钮样式

**样式参考**:
```css
/* Primary Button */
background: linear-gradient(135deg, #6366F1, #8B5CF6)
color: #FFFFFF
border-radius: 12px
padding: 12px 24px

/* Secondary Button */
background: rgba(99, 102, 241, 0.1)
color: #6366F1
border: 1px solid rgba(99, 102, 241, 0.3)
```

**测试用例**:
- [ ] 渲染不同类型的按钮
- [ ] 点击事件触发
- [ ] 禁用状态显示
- [ ] 加载状态显示
- [ ] 响应式大小

**预计时间**: 2-3 小时

---

### 任务 2: Input.tsx - 输入框组件

**文件位置**: `src/components/Common/Input.tsx`

**需求**:
```typescript
interface InputProps {
  type?: 'text' | 'password' | 'email' | 'number'
  placeholder?: string
  value?: string
  onChange?: (value: string) => void
  error?: string
  disabled?: boolean
  required?: boolean
  label?: string
}
```

**参考设计**: `dataviz_login.html` 中的输入框

**样式参考**:
```css
background: rgba(255, 255, 255, 0.4)
border: 1px solid rgba(255, 255, 255, 0.6)
border-radius: 12px
padding: 12px 16px
backdrop-filter: blur(12px)
```

**测试用例**:
- [ ] 输入文本
- [ ] 显示错误信息
- [ ] 禁用状态
- [ ] 必填标记
- [ ] 标签显示

**预计时间**: 2-3 小时

---

### 任务 3: Card.tsx - 卡片组件

**文件位置**: `src/components/Common/Card.tsx`

**需求**:
```typescript
interface CardProps {
  title?: string
  description?: string
  clickable?: boolean
  onClick?: () => void
  children?: React.ReactNode
  className?: string
}
```

**参考设计**: `dataviz_dashboard.html` 中的卡片

**样式参考**:
```css
background: rgba(255, 255, 255, 0.4)
border: 1px solid rgba(255, 255, 255, 0.6)
border-radius: 20px
padding: 24px
backdrop-filter: blur(12px)
box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08)
```

**测试用例**:
- [ ] 基础卡片渲染
- [ ] 标题和描述显示
- [ ] 可点击状态
- [ ] 悬停效果
- [ ] 自定义内容

**预计时间**: 2-3 小时

---

### 任务 4: Loading.tsx - 加载组件

**文件位置**: `src/components/Common/Loading.tsx`

**需求**:
```typescript
interface LoadingProps {
  type?: 'spinner' | 'skeleton' | 'fullscreen'
  message?: string
}
```

**样式参考**:
- 旋转加载动画
- 骨架屏加载
- 全屏加载覆盖层

**测试用例**:
- [ ] 显示加载动画
- [ ] 显示加载文本
- [ ] 全屏加载
- [ ] 骨架屏

**预计时间**: 2-3 小时

---

### 任务 5: LoginPage.tsx - 登录页面

**文件位置**: `src/pages/LoginPage.tsx`

**需求**:
- 用户名/邮箱输入
- 密码输入
- 记住密码复选框
- 登录按钮
- 注册链接
- 表单验证
- 错误提示

**参考设计**: `dataviz_login.html`

**功能**:
```typescript
// 表单状态
const [email, setEmail] = useState('')
const [password, setPassword] = useState('')
const [rememberMe, setRememberMe] = useState(false)
const [loading, setLoading] = useState(false)
const [error, setError] = useState('')

// 表单验证
const validateForm = () => {
  // 验证邮箱格式
  // 验证密码长度
  // 返回验证结果
}

// 提交表单
const handleSubmit = async () => {
  // 验证表单
  // 调用登录 API
  // 保存 Token
  // 重定向到 Dashboard
}
```

**测试用例**:
- [ ] 表单验证
- [ ] 错误提示
- [ ] 成功登录
- [ ] 记住密码
- [ ] 响应式设计

**预计时间**: 4-5 小时

---

### 任务 6: DashboardPage.tsx - 仪表板

**文件位置**: `src/pages/DashboardPage.tsx`

**需求**:
- 欢迎信息
- 数据统计卡片 (项目数、分析数等)
- 最近项目列表
- 快速操作按钮
- 响应式网格布局

**参考设计**: `dataviz_dashboard.html`

**功能**:
```typescript
// 获取仪表板数据
const [stats, setStats] = useState({
  totalProjects: 0,
  totalAnalyses: 0,
  recentProjects: []
})

// 加载数据
useEffect(() => {
  fetchDashboardData()
}, [])

// 快速操作
const handleNewProject = () => {
  // 导航到上传页面
}
```

**测试用例**:
- [ ] 数据加载
- [ ] 卡片显示
- [ ] 快速操作
- [ ] 项目列表
- [ ] 响应式设计

**预计时间**: 4-5 小时

---

### 任务 7: Navbar.tsx - 导航栏

**文件位置**: `src/components/Layout/Navbar.tsx`

**需求**:
- Logo 和应用名称
- 导航菜单
- 用户菜单 (头像、用户名、登出)
- 响应式汉堡菜单 (移动端)
- 玻璃拟态样式

**参考设计**: `dataviz_login.html` 中的导航栏

**功能**:
```typescript
// 导航项
const navItems = [
  { label: 'Dashboard', href: '/dashboard' },
  { label: 'Templates', href: '/templates' },
  { label: 'Upload', href: '/upload' },
  { label: 'Results', href: '/results' }
]

// 用户菜单
const handleLogout = () => {
  // 清除 Token
  // 重定向到登录页
}
```

**测试用例**:
- [ ] 导航链接
- [ ] 用户菜单
- [ ] 登出功能
- [ ] 响应式菜单
- [ ] 活跃状态

**预计时间**: 3-4 小时

---

### 任务 8: Sidebar.tsx - 侧边栏

**文件位置**: `src/components/Layout/Sidebar.tsx`

**需求**:
- 导航菜单
- 折叠/展开功能
- 活跃状态指示
- 响应式隐藏 (移动端)
- 玻璃拟态样式

**参考设计**: `dataviz_dashboard.html` 中的侧边栏

**功能**:
```typescript
// 菜单项
const menuItems = [
  { icon: '📊', label: 'Dashboard', href: '/dashboard' },
  { icon: '📋', label: 'Templates', href: '/templates' },
  { icon: '📤', label: 'Upload', href: '/upload' },
  { icon: '📈', label: 'Results', href: '/results' },
  { icon: '⚙️', label: 'Settings', href: '/settings' }
]

// 折叠状态
const [collapsed, setCollapsed] = useState(false)
```

**测试用例**:
- [ ] 菜单导航
- [ ] 折叠功能
- [ ] 活跃状态
- [ ] 响应式隐藏
- [ ] 图标显示

**预计时间**: 3-4 小时

---

### 任务 9: Modal.tsx - 模态框

**文件位置**: `src/components/Common/Modal.tsx`

**需求**:
```typescript
interface ModalProps {
  title: string
  isOpen: boolean
  onClose: () => void
  onConfirm?: () => void
  children: React.ReactNode
  confirmText?: string
  cancelText?: string
}
```

**样式参考**:
- 半透明背景
- 居中模态框
- 玻璃拟态样式
- 动画进入/退出

**测试用例**:
- [ ] 打开/关闭
- [ ] 确认/取消
- [ ] 自定义内容
- [ ] 动画效果
- [ ] 背景点击关闭

**预计时间**: 2-3 小时

---

### 任务 10: TemplatesPage.tsx - 模板选择

**文件位置**: `src/pages/TemplatesPage.tsx`

**需求**:
- 模板卡片网格
- 模板筛选/搜索
- 模板详情预览
- 选择确认
- 响应式设计

**参考设计**: `dataviz_templates.html`

**功能**:
```typescript
// 模板列表
const [templates, setTemplates] = useState([])
const [selectedTemplate, setSelectedTemplate] = useState(null)
const [filter, setFilter] = useState('')

// 加载模板
useEffect(() => {
  fetchTemplates()
}, [])

// 选择模板
const handleSelectTemplate = (template) => {
  setSelectedTemplate(template)
  // 导航到上传页面
}
```

**测试用例**:
- [ ] 模板加载
- [ ] 卡片显示
- [ ] 搜索筛选
- [ ] 模板选择
- [ ] 响应式设计

**预计时间**: 4-5 小时

---

### 任务 11: UploadPage.tsx - 数据上传

**文件位置**: `src/pages/UploadPage.tsx`

**需求**:
- 文件上传区域
- 进度条显示
- 数据预览表格
- 指标配置表单
- 上传确认

**参考设计**: `dataviz_upload.html`

**功能**:
```typescript
// 上传状态
const [file, setFile] = useState(null)
const [uploadProgress, setUploadProgress] = useState(0)
const [previewData, setPreviewData] = useState([])
const [metrics, setMetrics] = useState([])

// 处理文件上传
const handleFileUpload = async (file) => {
  // 解析 CSV/Excel
  // 显示预览
  // 上传到服务器
}

// 配置指标
const handleAddMetric = (metric) => {
  // 添加指标公式
  // 验证公式
}
```

**测试用例**:
- [ ] 文件上传
- [ ] 进度显示
- [ ] 数据预览
- [ ] 指标配置
- [ ] 上传确认

**预计时间**: 5-6 小时

---

### 任务 12: ResultsPage.tsx - 分析结果

**文件位置**: `src/pages/ResultsPage.tsx`

**需求**:
- 图表展示 (Recharts)
- AI 洞察文本
- 导出功能
- 分享功能
- 响应式设计

**参考设计**: `dataviz_results.html`

**功能**:
```typescript
// 结果数据
const [analysisResults, setAnalysisResults] = useState({
  charts: [],
  insights: '',
  metrics: {}
})

// 加载结果
useEffect(() => {
  fetchAnalysisResults()
}, [])

// 导出功能
const handleExport = (format) => {
  // 导出为 PDF/PNG/CSV
}

// 分享功能
const handleShare = () => {
  // 生成分享链接
}
```

**测试用例**:
- [ ] 图表显示
- [ ] 洞察文本
- [ ] 导出功能
- [ ] 分享功能
- [ ] 响应式设计

**预计时间**: 5-6 小时

---

### 任务 13: 全局样式

**文件位置**: `src/styles/globals.css`

**需求**:
- 全局字体设置
- 颜色变量定义
- 响应式断点
- 动画定义
- 工具类

**样式参考**:
```css
/* 颜色变量 */
--primary: #6366F1
--secondary: #8B5CF6
--background: linear-gradient(135deg, #F0F4F8 0%, #E8EEF5 50%, #DFE9F3 100%)

/* 响应式断点 */
--sm: 640px
--md: 768px
--lg: 1024px
--xl: 1280px

/* 动画 */
@keyframes fadeIn { ... }
@keyframes slideIn { ... }
```

**预计时间**: 2-3 小时

---

## 📊 进度追踪

| 任务 | 状态 | 完成度 | 预计时间 |
|------|------|--------|---------|
| 1. Button.tsx | ⏳ | 0% | 2-3h |
| 2. Input.tsx | ⏳ | 0% | 2-3h |
| 3. Card.tsx | ⏳ | 0% | 2-3h |
| 4. Loading.tsx | ⏳ | 0% | 2-3h |
| 5. LoginPage.tsx | ⏳ | 0% | 4-5h |
| 6. DashboardPage.tsx | ⏳ | 0% | 4-5h |
| 7. Navbar.tsx | ⏳ | 0% | 3-4h |
| 8. Sidebar.tsx | ⏳ | 0% | 3-4h |
| 9. Modal.tsx | ⏳ | 0% | 2-3h |
| 10. TemplatesPage.tsx | ⏳ | 0% | 4-5h |
| 11. UploadPage.tsx | ⏳ | 0% | 5-6h |
| 12. ResultsPage.tsx | ⏳ | 0% | 5-6h |
| 13. 全局样式 | ⏳ | 0% | 2-3h |
| **总计** | | **0%** | **40-50h** |

---

## 🚀 开始第一个任务

**建议从 Button.tsx 开始**:

1. 创建文件 `src/components/Common/Button.tsx`
2. 定义 Props 接口
3. 实现按钮组件
4. 添加 TailwindCSS 样式
5. 编写测试用例
6. 在其他组件中使用

**命令**:
```bash
# 启动开发服务器
npm run dev

# 在另一个终端运行测试
npm run test

# 代码格式化
npm run format
```

---

## 📚 参考资源

- React 官方文档: https://react.dev
- TypeScript 官方文档: https://www.typescriptlang.org
- TailwindCSS 官方文档: https://tailwindcss.com
- Vite 官方文档: https://vitejs.dev

---

**准备好开始了吗? 让我们开始第1阶段的开发! 🚀**

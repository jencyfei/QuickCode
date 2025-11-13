# 智能短信标签助手 - 前端

基于 Vue 3 + Vite + Vant 的移动端Web应用

## 🛠️ 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite 6
- **UI组件**: Vant 4.x (移动端)
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP客户端**: Axios

## 📦 项目结构

```
frontend/
├── src/
│   ├── api/              # API请求封装
│   │   ├── request.js    # Axios配置
│   │   └── auth.js       # 认证API
│   ├── assets/           # 静态资源
│   │   └── styles/       # 样式文件
│   ├── components/       # 公共组件
│   ├── router/           # 路由配置
│   ├── store/            # Pinia状态管理
│   │   ├── index.js      # Store入口
│   │   └── user.js       # 用户状态
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   │   ├── Login.vue     # 登录页
│   │   ├── SmsList.vue   # 短信列表
│   │   ├── SmsImport.vue # 短信导入
│   │   └── TagManage.vue # 标签管理
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html
├── vite.config.js        # Vite配置
└── package.json
```

## 🚀 快速开始

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问: http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 🎨 功能特性

### 已完成
- ✅ 项目基础架构
- ✅ Vue Router 路由配置
- ✅ Pinia 状态管理
- ✅ Axios 请求封装
- ✅ Vant UI 组件集成
- ✅ 移动端适配
- ✅ API代理配置
- ✅ 登录页面骨架
- ✅ 短信列表页面骨架
- ✅ 短信导入页面骨架
- ✅ 标签管理页面骨架

### 开发中
- 🔄 登录/注册功能完善
- 🔄 短信列表功能
- 🔄 短信导入与解析
- 🔄 标签管理功能
- 🔄 批量操作功能

## 📱 页面路由

| 路径 | 页面 | 说明 | 需要登录 |
|------|------|------|----------|
| `/` | - | 重定向到登录页 | ❌ |
| `/login` | Login.vue | 登录/注册页 | ❌ |
| `/sms-list` | SmsList.vue | 短信列表主页 | ✅ |
| `/sms-import` | SmsImport.vue | 短信导入页 | ✅ |
| `/tag-manage` | TagManage.vue | 标签管理页 | ✅ |

## 🔧 配置说明

### Vite 配置

- **端口**: 3000
- **API代理**: `/api` -> `http://localhost:8000`
- **路径别名**: `@` -> `src/`

### 移动端适配

使用 rem 单位进行移动端适配：
- 设计稿宽度: 750px
- 1rem = 100px

### 环境变量

创建 `.env.local` 文件配置环境变量：

```env
VITE_API_BASE_URL=http://localhost:8000
```

## 📝 开发规范

### 组件命名
- 页面组件: PascalCase (如 `SmsList.vue`)
- 公共组件: PascalCase (如 `SmsItem.vue`)

### API调用
```javascript
import { login } from '@/api/auth'

const response = await login({ username, password })
```

### 状态管理
```javascript
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
userStore.setToken(token)
```

### 路由跳转
```javascript
import { useRouter } from 'vue-router'

const router = useRouter()
router.push('/sms-list')
```

## 🐛 调试

### 查看API请求
打开浏览器开发者工具 -> Network 标签

### 查看Pinia状态
安装 Vue DevTools 浏览器扩展

## 📚 相关文档

- [Vue 3 文档](https://cn.vuejs.org/)
- [Vite 文档](https://cn.vitejs.dev/)
- [Vant 文档](https://vant-ui.github.io/vant/)
- [Vue Router 文档](https://router.vuejs.org/zh/)
- [Pinia 文档](https://pinia.vuejs.org/zh/)

## 🤝 贡献

遵循项目的开发原则：
1. 页面图先行
2. 移动端优先
3. 增量式开发
4. 测试驱动

---

**当前版本**: v0.1.0 (MVP开发中)

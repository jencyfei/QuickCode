# DataViz Insights 实施计划 - 阶段1前期

**文档版本**: 1.0  
**创建日期**: 2025年11月17日  
**阶段**: 原型设计与架构规划  
**周期**: 2周（第1-2周）  
**目标**: 完成Figma原型 + 技术栈搭建 + API文档

---

## 📋 任务总览

### 完成的文档

1. ✅ **2.prototype_design_part1.md** - 原型设计方案
   - 5个关键页面设计（登录、Dashboard、模板选择、数据上传、结果展示）
   - 设计系统规范（色彩、排版、响应式）
   - Figma任务清单
   - 技术栈搭建方案

2. ✅ **3.api_design.md** - API设计文档
   - 12个API模块（认证、模板、数据、分析、AI、导出、用户、订阅）
   - 完整的请求/响应示例
   - 错误码参考
   - 速率限制规则

3. ✅ **4.database_design.md** - 数据库设计文档
   - 8个核心表设计（users、templates、uploads、analyses等）
   - 详细的SQL脚本
   - 查询优化建议
   - 备份恢复策略

---

## 🎯 第1周任务（原型设计）

### 周一-周二: Figma设计系统搭建

**目标**: 建立设计系统库，确保设计一致性

**任务**:
- [ ] 创建Figma项目 "DataViz Insights"
- [ ] 建立色彩系统库
  - 主色: #6366F1 (Indigo 600)
  - 辅色: #EC4899 (Pink 500)
  - 成功/警告/错误色
- [ ] 建立排版系统库
  - Poppins (标题)
  - Inter (正文)
  - Fira Code (代码)
- [ ] 建立组件库
  - 按钮（主/次/危险）
  - 输入框（文本/密码/搜索）
  - 卡片、标签、模态框等

**交付物**: Figma设计系统库链接

**时间**: 8小时

---

### 周二-周三: 高保真页面设计

**目标**: 完成5个关键页面的高保真设计

**任务**:
- [ ] 登录/注册页设计
  - PC布局 (1920px)
  - 手机布局 (375px)
  - 交互说明
  
- [ ] Dashboard页设计
  - 历史分析卡片
  - 模板库展示
  - 快速操作按钮
  
- [ ] 模板选择页设计
  - 分类标签
  - 模板卡片网格
  - 预览弹窗
  
- [ ] 数据上传页设计
  - 拖拽上传区
  - 指标编辑器
  - AI建议面板
  
- [ ] 结果展示页设计
  - 图表展示区
  - Insights面板
  - 导出/分享按钮

**交付物**: 5个页面的高保真设计 + 交互原型

**时间**: 16小时

---

### 周四: 设计规范文档

**目标**: 生成Figma设计规范，供开发者参考

**任务**:
- [ ] 导出设计规范 (Figma Specs)
- [ ] 生成设计资源包
  - 图标库 (SVG)
  - 颜色代码
  - 字体文件
- [ ] 编写UI组件使用说明
- [ ] 创建设计交接文档

**交付物**: 设计规范文档 + 资源包

**时间**: 8小时

---

## 🎯 第2周任务（技术栈搭建）

### 周一: 前端项目搭建

**目标**: 建立React + TypeScript + TailwindCSS项目骨架

**任务**:
```bash
# 创建项目
npm create vite@latest dataviz-frontend -- --template react-ts
cd dataviz-frontend

# 安装依赖
npm install -D tailwindcss postcss autoprefixer
npm install axios zustand react-query recharts
npm install -D typescript @types/react @types/node

# 初始化TailwindCSS
npx tailwindcss init -p

# 项目结构
src/
├── components/        # React组件
├── pages/            # 页面
├── hooks/            # 自定义Hook
├── stores/           # Zustand状态
├── services/         # API调用
├── utils/            # 工具函数
├── styles/           # TailwindCSS
└── App.tsx
```

**交付物**: 前端项目骨架 + package.json

**时间**: 6小时

---

### 周二: 后端项目搭建

**目标**: 建立FastAPI项目骨架

**任务**:
```bash
# 创建项目
mkdir dataviz-backend
cd dataviz-backend

# 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 安装依赖
pip install fastapi uvicorn pandas numpy scikit-learn
pip install sqlalchemy psycopg2-binary python-dotenv
pip install pydantic pydantic-settings
pip install pytest pytest-asyncio

# 项目结构
app/
├── main.py           # FastAPI入口
├── models/           # SQLAlchemy模型
├── routers/          # API路由
├── schemas/          # Pydantic模型
├── services/         # 业务逻辑
├── utils/            # 工具函数
├── config.py         # 配置
└── database.py       # 数据库连接

tests/
├── test_auth.py
├── test_analysis.py
└── conftest.py

requirements.txt
Dockerfile
.env.example
```

**交付物**: 后端项目骨架 + requirements.txt

**时间**: 6小时

---

### 周三: 数据库初始化

**目标**: 创建PostgreSQL数据库和所有表

**任务**:
- [ ] 安装PostgreSQL 14+
- [ ] 创建数据库
  ```sql
  CREATE DATABASE dataviz_insights;
  ```
- [ ] 执行schema.sql脚本
  ```bash
  psql -U postgres -d dataviz_insights -f schema.sql
  ```
- [ ] 验证表结构
- [ ] 配置备份策略

**交付物**: PostgreSQL数据库 + schema.sql

**时间**: 4小时

---

### 周四: API文档与Mock服务

**目标**: 完成API文档 + 创建Mock服务供前端开发

**任务**:
- [ ] 在FastAPI中配置Swagger UI
  ```python
  app = FastAPI(
    title="DataViz Insights API",
    description="数据分析SaaS API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
  )
  ```
- [ ] 编写API路由框架（不实现业务逻辑）
- [ ] 创建Mock数据服务
- [ ] 生成API文档 (Swagger JSON)
- [ ] 配置CORS允许前端调用

**交付物**: Swagger API文档 + Mock服务

**时间**: 8小时

---

### 周五: 集成与测试

**目标**: 验证前后端集成，确保开发环境就绪

**任务**:
- [ ] 配置前端API客户端
  ```typescript
  // services/api.ts
  import axios from 'axios';
  
  const api = axios.create({
    baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8000/v1',
    timeout: 10000,
  });
  ```
- [ ] 测试前后端通信
- [ ] 编写集成测试
- [ ] 配置Docker Compose
  ```yaml
  version: '3.8'
  services:
    backend:
      build: ./backend
      ports:
        - "8000:8000"
    frontend:
      build: ./frontend
      ports:
        - "3000:3000"
    postgres:
      image: postgres:14
      environment:
        POSTGRES_DB: dataviz_insights
  ```
- [ ] 验证本地开发环境

**交付物**: 集成测试通过 + Docker Compose配置

**时间**: 6小时

---

## 📊 时间分配总结

| 阶段 | 任务 | 时间 | 状态 |
|------|------|------|------|
| 第1周 | Figma设计系统 | 8h | ⏳ |
| 第1周 | 高保真页面设计 | 16h | ⏳ |
| 第1周 | 设计规范文档 | 8h | ⏳ |
| 第2周 | 前端项目搭建 | 6h | ⏳ |
| 第2周 | 后端项目搭建 | 6h | ⏳ |
| 第2周 | 数据库初始化 | 4h | ⏳ |
| 第2周 | API文档与Mock | 8h | ⏳ |
| 第2周 | 集成与测试 | 6h | ⏳ |
| **总计** | | **62h** | |

**周工作量**: 30-32小时/周（合理的全职工作量）

---

## 📦 交付物清单

### 第1周交付物

1. ✅ Figma设计系统库
   - 色彩系统
   - 排版系统
   - 组件库

2. ✅ 5个页面高保真设计
   - 登录/注册页
   - Dashboard页
   - 模板选择页
   - 数据上传页
   - 结果展示页

3. ✅ 设计规范文档
   - UI组件使用说明
   - 设计资源包（图标、颜色、字体）

### 第2周交付物

1. ✅ 前端项目骨架
   - React + TypeScript + TailwindCSS
   - 项目结构
   - 依赖配置

2. ✅ 后端项目骨架
   - FastAPI项目
   - 项目结构
   - 依赖配置

3. ✅ PostgreSQL数据库
   - 8个核心表
   - 索引和视图
   - schema.sql脚本

4. ✅ API文档
   - Swagger UI
   - API路由框架
   - Mock数据服务

5. ✅ 开发环境配置
   - Docker Compose
   - 集成测试
   - 本地开发指南

---

## 🚀 后续阶段（第3-6周）

### 第3-4周: 核心功能开发

**前端**:
- 实现登录/注册页面
- 实现Dashboard页面
- 实现数据上传页面
- 集成API调用

**后端**:
- 实现用户认证API
- 实现数据上传处理
- 实现数据处理引擎
- 实现图表推荐规则

### 第5-6周: AI与优化

**前端**:
- 实现结果展示页面
- 实现图表渲染
- 实现导出/分享功能

**后端**:
- 集成OpenAI GPT
- 实现Insights生成
- 性能优化
- 单元测试

---

## ✅ 成功标准

### 第1周成功标准

- [ ] Figma原型设计完成，包含5个页面
- [ ] 设计系统库建立，包含色彩、排版、组件
- [ ] 设计规范文档完成
- [ ] 所有设计资源导出完成

### 第2周成功标准

- [ ] 前端项目可以正常启动 (`npm run dev`)
- [ ] 后端项目可以正常启动 (`uvicorn app.main:app --reload`)
- [ ] PostgreSQL数据库创建成功，所有表存在
- [ ] Swagger API文档可以访问 (`http://localhost:8000/docs`)
- [ ] 前后端可以通过API通信
- [ ] Docker Compose可以启动所有服务
- [ ] 集成测试通过

---

## 📝 检查清单

### 开发前准备

- [ ] 安装Figma
- [ ] 安装Node.js 18+
- [ ] 安装Python 3.11+
- [ ] 安装PostgreSQL 14+
- [ ] 安装Docker & Docker Compose
- [ ] 安装Git
- [ ] 创建GitHub仓库

### 第1周检查

- [ ] Figma项目已创建并邀请团队成员
- [ ] 5个页面设计已完成
- [ ] 设计系统库已建立
- [ ] 设计规范文档已生成

### 第2周检查

- [ ] 前端项目可以启动
- [ ] 后端项目可以启动
- [ ] 数据库已创建
- [ ] API文档可以访问
- [ ] Mock服务可以调用
- [ ] Docker Compose可以启动

---

## 🔗 相关文档

- `1.task.md` - 产品需求文档 (PRD)
- `2.prototype_design_part1.md` - 原型设计方案
- `3.api_design.md` - API设计文档
- `4.database_design.md` - 数据库设计文档

---

## 📞 支持与反馈

如有任何问题或建议，请：

1. 创建GitHub Issue
2. 发送邮件至 support@dataviz-insights.com
3. 在Figma中评论设计

---

**下一步**: 确认是否准备开始第1周的Figma设计工作

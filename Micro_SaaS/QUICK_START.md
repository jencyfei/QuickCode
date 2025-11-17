# DataViz Insights 快速开始指南

**最后更新**: 2025年11月17日

---

## 📚 文档导航

### 产品规划
- **1.task.md** - 完整的产品需求文档 (PRD)
  - 产品背景、目标用户、功能需求
  - 商业模式、市场定位、成功指标

### 设计与架构
- **2.prototype_design_part1.md** - 原型设计方案
  - 5个关键页面设计（PC + 手机）
  - 设计系统规范（色彩、排版、响应式）
  - Figma任务清单
  - 技术栈建议

- **3.api_design.md** - API设计文档
  - 12个API模块的完整设计
  - 请求/响应示例
  - 错误码参考
  - 速率限制规则

- **4.database_design.md** - 数据库设计文档
  - 8个核心表的SQL脚本
  - 查询优化建议
  - 备份恢复策略
  - 性能监控方案

### 实施计划
- **IMPLEMENTATION_PLAN.md** - 详细的2周实施计划
  - 第1周: Figma设计（32小时）
  - 第2周: 技术栈搭建（30小时）
  - 交付物清单
  - 成功标准

---

## 🎯 立即开始（本周行动）

### Step 1: 环境准备（2小时）

```bash
# 1. 安装必要工具
# - Node.js 18+ (https://nodejs.org)
# - Python 3.11+ (https://python.org)
# - PostgreSQL 14+ (https://postgresql.org)
# - Docker & Docker Compose (https://docker.com)
# - Git (https://git-scm.com)
# - Figma (https://figma.com)

# 2. 创建GitHub仓库
git clone https://github.com/your-username/dataviz-insights.git
cd dataviz-insights
git init

# 3. 创建项目目录结构
mkdir frontend backend docs
touch README.md .gitignore
```

### Step 2: Figma设计（第1周，32小时）

```
周一-周二 (8h): 建立设计系统库
  ✓ 创建Figma项目
  ✓ 建立色彩系统 (#6366F1, #EC4899等)
  ✓ 建立排版系统 (Poppins, Inter, Fira Code)
  ✓ 建立组件库 (按钮、输入框、卡片等)

周二-周三 (16h): 高保真页面设计
  ✓ 登录/注册页 (PC + 手机)
  ✓ Dashboard页 (PC + 手机)
  ✓ 模板选择页 (PC + 手机)
  ✓ 数据上传页 (PC + 手机)
  ✓ 结果展示页 (PC + 手机)

周四 (8h): 设计规范与交接
  ✓ 导出设计规范 (Figma Specs)
  ✓ 导出设计资源 (图标、颜色、字体)
  ✓ 编写UI组件使用说明
```

**关键设计参数**:
- PC宽度: 1920px
- 手机宽度: 375px
- 主色: #6366F1 (Indigo 600)
- 辅色: #EC4899 (Pink 500)
- 字体: Poppins (标题) + Inter (正文)

### Step 3: 前端项目搭建（第2周周一，6小时）

```bash
# 创建React项目
npm create vite@latest dataviz-frontend -- --template react-ts
cd dataviz-frontend

# 安装依赖
npm install -D tailwindcss postcss autoprefixer
npm install axios zustand react-query recharts
npm install -D typescript @types/react @types/node

# 初始化TailwindCSS
npx tailwindcss init -p

# 启动开发服务器
npm run dev
# 访问: http://localhost:5173
```

**项目结构**:
```
dataviz-frontend/
├── src/
│   ├── components/      # React组件
│   ├── pages/          # 页面
│   ├── hooks/          # 自定义Hook
│   ├── stores/         # Zustand状态
│   ├── services/       # API调用
│   ├── utils/          # 工具函数
│   ├── styles/         # TailwindCSS
│   └── App.tsx
├── package.json
├── vite.config.ts
└── tsconfig.json
```

### Step 4: 后端项目搭建（第2周周二，6小时）

```bash
# 创建项目目录
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

# 创建requirements.txt
pip freeze > requirements.txt

# 启动开发服务器
uvicorn app.main:app --reload
# 访问: http://localhost:8000/docs
```

**项目结构**:
```
dataviz-backend/
├── app/
│   ├── main.py         # FastAPI入口
│   ├── models/         # SQLAlchemy模型
│   ├── routers/        # API路由
│   ├── schemas/        # Pydantic模型
│   ├── services/       # 业务逻辑
│   ├── utils/          # 工具函数
│   ├── config.py       # 配置
│   └── database.py     # 数据库连接
├── tests/
├── requirements.txt
├── Dockerfile
└── .env.example
```

### Step 5: 数据库初始化（第2周周三，4小时）

```bash
# 创建数据库
psql -U postgres -c "CREATE DATABASE dataviz_insights;"

# 执行schema.sql脚本
psql -U postgres -d dataviz_insights -f schema.sql

# 验证表结构
psql -U postgres -d dataviz_insights -c "\dt"
```

**核心表**:
- users - 用户表
- templates - 模板表
- uploads - 数据上传表
- analyses - 分析表
- analysis_results - 分析结果表
- shares - 分享表
- subscriptions - 订阅表
- audit_logs - 审计日志表

### Step 6: API文档与Mock（第2周周四，8小时）

```python
# app/main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="DataViz Insights API",
    description="数据分析SaaS API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 访问API文档: http://localhost:8000/docs
```

**API模块**:
- `/auth` - 认证
- `/templates` - 模板
- `/data` - 数据处理
- `/analysis` - 分析
- `/ai` - AI建议
- `/export` - 导出分享
- `/user` - 用户设置
- `/subscription` - 订阅

### Step 7: 集成与验证（第2周周五，6小时）

```bash
# 创建docker-compose.yml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    environment:
      DATABASE_URL: postgresql://postgres:password@postgres:5432/dataviz_insights
  
  frontend:
    build: ./frontend
    ports:
      - "5173:5173"
  
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: dataviz_insights
      POSTGRES_PASSWORD: password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:

# 启动所有服务
docker-compose up

# 验证
# - 前端: http://localhost:5173
# - 后端: http://localhost:8000
# - API文档: http://localhost:8000/docs
```

---

## 📊 关键指标

### 第1周目标
- ✅ Figma原型完成（5个页面）
- ✅ 设计系统库建立
- ✅ 设计规范文档完成

### 第2周目标
- ✅ 前端项目可启动
- ✅ 后端项目可启动
- ✅ 数据库已创建
- ✅ API文档可访问
- ✅ Docker Compose可启动

### 总工作量
- 第1周: 32小时
- 第2周: 30小时
- **总计: 62小时**（2周全职）

---

## 🔧 常用命令

### 前端开发

```bash
cd dataviz-frontend

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产版本
npm run preview

# 运行测试
npm run test

# 代码检查
npm run lint
```

### 后端开发

```bash
cd dataviz-backend

# 启动开发服务器
uvicorn app.main:app --reload

# 运行测试
pytest

# 生成数据库迁移
alembic revision --autogenerate -m "description"

# 应用迁移
alembic upgrade head
```

### 数据库管理

```bash
# 连接数据库
psql -U postgres -d dataviz_insights

# 备份数据库
pg_dump -U postgres dataviz_insights > backup.sql

# 恢复数据库
psql -U postgres dataviz_insights < backup.sql

# 查看表结构
\dt

# 查看表详情
\d table_name
```

---

## 📖 学习资源

### 前端
- React官方文档: https://react.dev
- TailwindCSS: https://tailwindcss.com
- shadcn/ui: https://ui.shadcn.com
- Recharts: https://recharts.org

### 后端
- FastAPI: https://fastapi.tiangolo.com
- SQLAlchemy: https://sqlalchemy.org
- Pandas: https://pandas.pydata.org
- Pydantic: https://docs.pydantic.dev

### 设计
- Figma官方教程: https://help.figma.com
- 设计系统最佳实践: https://www.designsystems.com

### 部署
- Vercel: https://vercel.com
- Railway: https://railway.app
- AWS Lambda: https://aws.amazon.com/lambda
- Docker: https://docker.com

---

## ❓ 常见问题

### Q: 如何快速启动开发环境？
A: 使用Docker Compose一键启动所有服务:
```bash
docker-compose up
```

### Q: 前端如何调用后端API？
A: 使用axios + React Query:
```typescript
import { useQuery } from 'react-query';
import api from '@/services/api';

const { data } = useQuery('analyses', () => api.get('/analysis'));
```

### Q: 如何添加新的API端点？
A: 在FastAPI中创建新的路由:
```python
@app.get("/api/endpoint")
async def endpoint():
    return {"message": "Hello"}
```

### Q: 如何修改数据库表结构？
A: 使用Alembic进行迁移:
```bash
alembic revision --autogenerate -m "Add new column"
alembic upgrade head
```

---

## 📞 获取帮助

- 📖 查看完整文档: 见上面的"文档导航"
- 🐛 报告问题: 创建GitHub Issue
- 💬 讨论想法: 在GitHub Discussions中讨论
- 📧 联系支持: support@dataviz-insights.com

---

## ✅ 下一步检查清单

- [ ] 所有工具已安装
- [ ] GitHub仓库已创建
- [ ] 项目目录结构已建立
- [ ] 第1周Figma设计已开始
- [ ] 第2周技术栈搭建计划已确认

**准备好开始了吗？** 🚀

从Figma设计开始，或者查看 `IMPLEMENTATION_PLAN.md` 了解详细的周计划。

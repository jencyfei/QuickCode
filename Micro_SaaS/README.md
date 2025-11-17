# DataViz Insights - 数据分析SaaS平台

![Status](https://img.shields.io/badge/status-planning-yellow)
![Version](https://img.shields.io/badge/version-1.0.0-blue)
![License](https://img.shields.io/badge/license-MIT-green)

## 📋 项目概述

**DataViz Insights** 是一个低门槛数据可视化SaaS平台，致力于帮助中小企业主、营销人员和自由分析师快速生成专业的数据分析报告。

### 核心价值
- **输入简单**: 用户仅需上传表结构 + 定义指标公式
- **输出专业**: 系统自动生成交互式图表 + AI驱动的insights
- **模板库**: 内置行业模板（抖音电商、外卖平台等）
- **AI辅助**: 自动推荐指标和图表类型

### 商业模式
- **免费版**: 5个分析/月
- **基本版**: $9/月（无限分析 + 模板库）
- **高级版**: $19/月（AI insights + 批量处理 + 导出 + 推送）

---

## 📁 项目文档

### 核心文档
| 文档 | 描述 | 大小 |
|------|------|------|
| **1.task.md** | 完整的产品需求文档 (PRD) | 15KB |
| **2.prototype_design_part1.md** | 原型设计方案（5个页面） | 25KB |
| **3.api_design.md** | API设计文档（12个模块） | 20KB |
| **4.database_design.md** | 数据库设计（8个表） | 22KB |
| **IMPLEMENTATION_PLAN.md** | 详细的2周实施计划 | 18KB |
| **QUICK_START.md** | 快速开始指南 | 16KB |

### 文档导航

#### 📖 产品规划
- **1.task.md** - 产品背景、目标用户、功能需求、商业模式、开发规划

#### 🎨 设计与架构
- **2.prototype_design_part1.md** - UI/UX设计、Figma任务、技术栈建议
- **3.api_design.md** - RESTful API设计、请求/响应示例、错误处理
- **4.database_design.md** - 数据库表设计、SQL脚本、性能优化

#### 🚀 实施指南
- **IMPLEMENTATION_PLAN.md** - 2周详细计划、交付物清单、成功标准
- **QUICK_START.md** - 快速开始、常用命令、学习资源

---

## 🎯 项目阶段

### ✅ 阶段0: 需求分析（已完成）
- [x] 产品需求文档编写
- [x] 用户画像分析
- [x] 市场定位确认
- [x] 商业模式设计

### ⏳ 阶段1: 原型设计与架构（进行中）
- [ ] **第1周**: Figma设计系统 + 高保真页面（32小时）
  - 建立设计系统库
  - 完成5个页面高保真设计
  - 生成设计规范文档
  
- [ ] **第2周**: 技术栈搭建（30小时）
  - 前端项目搭建（React + TypeScript + TailwindCSS）
  - 后端项目搭建（FastAPI + PostgreSQL）
  - 数据库初始化
  - API文档与Mock服务
  - 集成测试

### 📅 阶段2: 核心功能开发（第3-4周）
- [ ] 用户认证系统
- [ ] 数据上传处理
- [ ] 图表生成引擎
- [ ] AI insights生成

### 📅 阶段3: Beta测试与迭代（第5-6周）
- [ ] 招募Beta用户
- [ ] 收集反馈迭代
- [ ] 支付集成
- [ ] 性能优化

### 🚀 阶段4: 上线与增长（第7-8周）
- [ ] 生产部署
- [ ] 营销启动
- [ ] 用户获取

### 📱 阶段5: 手机端扩展（v2, 第9-14周）
- [ ] Android原生app或PWA
- [ ] 移动优化
- [ ] 推送通知

---

## 🛠️ 技术栈

### 前端
```
React 18.x + TypeScript
├── UI框架: TailwindCSS
├── 图表库: Recharts
├── 状态管理: Zustand
├── HTTP客户端: Axios + React Query
└── 构建工具: Vite
```

### 后端
```
Python 3.11 + FastAPI
├── 数据处理: Pandas + NumPy
├── 图表推荐: scikit-learn
├── AI/LLM: OpenAI GPT-4
├── 数据库ORM: SQLAlchemy
└── 异步框架: Uvicorn
```

### 数据库
```
PostgreSQL 14+
├── 用户数据
├── 分析结果
├── 模板库
└── 审计日志
```

### 部署
```
前端: Vercel / Netlify
后端: Railway / AWS Lambda
容器: Docker + Docker Compose
```

---

## 📊 关键设计参数

### 色彩系统
- **主色**: #6366F1 (Indigo 600) - 按钮、链接
- **辅色**: #EC4899 (Pink 500) - 强调、警告
- **成功**: #10B981 (Emerald 500)
- **警告**: #F59E0B (Amber 500)
- **错误**: #EF4444 (Red 500)

### 排版系统
- **标题**: Poppins Bold (32px/28px/24px/20px)
- **正文**: Inter Regular (16px/14px/12px)
- **代码**: Fira Code Regular (13px)

### 响应式断点
- **Desktop**: 1920px (PC优先，70%用户)
- **Tablet**: 768px
- **Mobile**: 375px (手机优先，30%用户)

---

## 📈 成功指标

### 用户指标
- 注册转化率 > 20%
- 月活跃用户 (MAU) > 100
- 用户留存率 (Day 30) > 40%
- Churn率 < 15%

### 商业指标
- 首月MRR $500
- 首3月MRR $1,000
- 付费用户转化率 > 10%
- 复购率 > 70%

### 产品指标
- 图表生成时间 < 2秒
- AI建议生成时间 < 5秒
- 指标建议准确率 > 90%
- 系统可用性 > 99.5%

---

## 🚀 快速开始

### 前置要求
- Node.js 18+
- Python 3.11+
- PostgreSQL 14+
- Docker & Docker Compose
- Git

### 5分钟快速启动

```bash
# 1. 克隆仓库
git clone https://github.com/your-username/dataviz-insights.git
cd dataviz-insights

# 2. 启动所有服务
docker-compose up

# 3. 访问应用
# 前端: http://localhost:5173
# 后端: http://localhost:8000
# API文档: http://localhost:8000/docs
```

### 详细开发指南
见 `QUICK_START.md`

---

## 📚 API文档

### 认证
```
POST /v1/auth/register       - 用户注册
POST /v1/auth/login          - 用户登录
POST /v1/auth/oauth          - OAuth登录
```

### 模板
```
GET /v1/templates            - 获取模板列表
GET /v1/templates/{id}       - 获取模板详情
```

### 数据处理
```
POST /v1/data/upload         - 上传数据文件
GET /v1/data/{id}/preview    - 获取数据预览
```

### 分析
```
POST /v1/analysis            - 创建分析
GET /v1/analysis/{id}        - 获取分析结果
GET /v1/analysis             - 获取分析列表
DELETE /v1/analysis/{id}     - 删除分析
```

### AI建议
```
POST /v1/ai/suggest-metrics  - 获取指标建议
POST /v1/ai/recommend-charts - 获取图表推荐
```

### 导出分享
```
POST /v1/export/{id}         - 导出分析
POST /v1/share/{id}          - 生成分享链接
GET /v1/share/{id}           - 访问分享链接
```

完整API文档见 `3.api_design.md`

---

## 📋 数据库表

| 表名 | 描述 | 行数 |
|------|------|------|
| users | 用户表 | ~1K |
| templates | 模板表 | ~20 |
| uploads | 数据上传表 | ~10K |
| analyses | 分析表 | ~50K |
| analysis_results | 分析结果表 | ~50K |
| shares | 分享表 | ~10K |
| subscriptions | 订阅表 | ~1K |
| audit_logs | 审计日志表 | ~100K |

完整数据库设计见 `4.database_design.md`

---

## 🔐 安全特性

- ✅ 密码加密 (bcrypt + salt)
- ✅ 数据加密 (AES-256)
- ✅ JWT认证
- ✅ HTTPS/TLS 1.3
- ✅ 行级安全 (RLS)
- ✅ GDPR/CCPA合规
- ✅ 审计日志

---

## 📊 项目统计

| 指标 | 数值 |
|------|------|
| 文档数量 | 6份 |
| 总文档大小 | ~116KB |
| API端点数 | 30+ |
| 数据库表数 | 8个 |
| 页面设计 | 5个 |
| 预计工作量 | 62小时 (2周) |
| 预计总周期 | 14周 |

---

## 🤝 贡献指南

### 开发流程
1. 创建特性分支 (`git checkout -b feature/xxx`)
2. 提交更改 (`git commit -am 'Add feature'`)
3. 推送到分支 (`git push origin feature/xxx`)
4. 创建Pull Request

### 代码规范
- 前端: ESLint + Prettier
- 后端: Black + isort + flake8
- 数据库: 遵循SQL最佳实践

### 测试要求
- 前端: Jest + React Testing Library
- 后端: pytest + pytest-asyncio
- 覆盖率: > 80%

---

## 📞 支持

- 📖 **文档**: 查看 `QUICK_START.md` 和各模块文档
- 🐛 **问题**: 创建GitHub Issue
- 💬 **讨论**: 在GitHub Discussions中讨论
- 📧 **联系**: support@dataviz-insights.com

---

## 📄 许可证

MIT License - 详见 LICENSE 文件

---

## 🎯 下一步

1. **确认设计方案** - 审核 `2.prototype_design_part1.md`
2. **启动Figma设计** - 按照 `IMPLEMENTATION_PLAN.md` 第1周计划
3. **搭建技术栈** - 按照 `IMPLEMENTATION_PLAN.md` 第2周计划
4. **开发核心功能** - 第3-4周开始实现

---

## 📈 项目进度

```
阶段0: 需求分析          ████████████████████ 100% ✅
阶段1: 原型设计与架构    ████░░░░░░░░░░░░░░░░  20% ⏳
阶段2: 核心功能开发      ░░░░░░░░░░░░░░░░░░░░   0% ⏳
阶段3: Beta测试与迭代    ░░░░░░░░░░░░░░░░░░░░   0% ⏳
阶段4: 上线与增长        ░░░░░░░░░░░░░░░░░░░░   0% ⏳
阶段5: 手机端扩展        ░░░░░░░░░░░░░░░░░░░░   0% ⏳
```

---

**最后更新**: 2025年11月17日  
**项目状态**: 🟡 规划中  
**下一个里程碑**: 第1周Figma设计完成 (2025年11月24日)

# 📱 SMS Agent - 智能短信助手

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Android-lightgrey.svg)

一个基于 Android + FastAPI 的智能短信管理应用，专注于短信读取与快递取件码提取等核心功能。

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目结构](#-项目结构)

</div>

---

## ✨ 功能特性

### 📦 快递管理
- **自动提取取件码** - 从短信中智能识别并提取快递取件码
- **日期分组显示** - 按日期自动分组，支持折叠/展开
- **快速标记** - 一键标记已取件，支持批量操作
- **搜索筛选** - 支持取件码和日期搜索
- **多种快递平台支持** - 菜鸟驿站、兔喜生活、京东物流等

### 💬 短信管理
- **短信列表** - 清晰展示所有短信，按时间倒序排列
- **搜索功能** - 支持短信内容搜索

### 🎨 用户体验
- **玻璃拟态设计** - 现代化的 UI 设计风格
- **流畅动画** - 丰富的交互动画效果
- **响应式布局** - 适配不同屏幕尺寸
- **夜间模式** - 支持深色主题（规划中）

---

## 🏗️ 技术栈

### Android 端
- **语言**: Kotlin
- **UI 框架**: Jetpack Compose
- **架构模式**: MVVM
- **最小 SDK**: 23 (Android 6.0)
- **目标 SDK**: 34 (Android 14)

### 后端 API
- **框架**: FastAPI
- **语言**: Python 3.10+
- **数据库**: PostgreSQL
- **ORM**: SQLAlchemy 2.0+
- **认证**: JWT

### 前端（Web）
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Vant 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

---

## 📁 项目结构

```
sms_agent/
├── android/                 # Android 原生应用
│   ├── app/
│   │   └── src/main/java/com/sms/tagger/
│   │       ├── ui/screens/  # UI 界面
│   │       │   ├── ExpressScreen.kt      # 快递管理页面
│   │       │   ├── SmsListScreen.kt      # 短信列表页面
│   │       │   └── SettingsScreen.kt     # 设置页面
│   │       └── util/        # 工具类
│   │           ├── SmsReader.kt          # 短信读取
│   │           └── ExpressExtractor.kt   # 快递信息提取
│   └── build.gradle
│
├── backend/                 # 后端 API 服务
│   ├── app/
│   │   ├── main.py          # FastAPI 应用入口
│   │   ├── routers/         # API 路由
│   │   │   ├── auth.py      # 认证相关
│   │   │   ├── sms.py       # 短信管理
│   │   │   └── extraction_rules.py  # 规则管理
│   │   ├── models/          # 数据模型
│   │   ├── schemas/         # Pydantic 模式
│   │   ├── services/        # 业务逻辑
│   │   │   ├── sms_classifier.py    # 短信分类器
│   │   │   └── rule_engine.py       # 规则引擎
│   │   └── database.py      # 数据库配置
│   └── requirements.txt
│
├── frontend/                # Web 前端应用
│   ├── src/
│   │   ├── views/           # 页面组件
│   │   ├── components/      # 公共组件
│   │   ├── api/             # API 封装
│   │   └── store/           # 状态管理
│   └── package.json
│
└── scripts/                 # 工具脚本
    └── init_db.py           # 数据库初始化
```

---

## 🚀 快速开始

### 前置要求

#### Android 开发
- **JDK**: 8 或更高版本
- **Android Studio**: Hedgehog | 2023.1.1+ 或更高版本
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+

#### 后端开发
- **Python**: 3.10 或更高版本
- **PostgreSQL**: 12 或更高版本
- **pip**: 最新版本

#### 前端开发
- **Node.js**: 16 或更高版本
- **npm**: 8+ 或 **yarn**: 1.22+

### 系统要求

- **Android 设备**: Android 6.0 (API 23) 或更高版本
- **存储空间**: 至少 50MB 可用空间
- **网络**: 可选（用于 API 调用，本地使用不需要）

### Android 应用安装

#### 方式一：直接安装 APK（推荐）

```bash
# 下载最新版本的 APK 文件
# 使用 adb 安装到设备
adb install app-release-20251120-v1.3-search-input-fix.apk
```

#### 方式二：从源码构建

```bash
# 1. 克隆项目
git clone https://github.com/your-username/sms_agent.git
cd sms_agent

# 2. 打开 Android Studio
# 3. 打开 android 目录作为项目
# 4. 等待 Gradle 同步完成
# 5. 连接 Android 设备或启动模拟器
# 6. 点击运行按钮或使用快捷键 Shift+F10

# 或者使用命令行构建
cd android
./gradlew assembleRelease  # Linux/Mac
.\gradlew.bat assembleRelease  # Windows

# APK 文件位置: app/build/outputs/apk/release/app-release.apk
```

**首次运行前**:
1. 确保已授予短信读取权限
2. 允许应用在后台运行（可选）
3. 允许应用通知权限（可选）

### 后端服务部署

```bash
# 1. 进入后端目录
cd backend

# 2. 创建虚拟环境
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# 3. 安装依赖
pip install -r requirements.txt

# 4. 配置环境变量
# 创建 .env 文件，配置以下信息：
# DATABASE_URL=postgresql://user:password@localhost:5432/sms_agent
# SECRET_KEY=your-secret-key-here
# DEBUG=True

# 5. 初始化数据库
python scripts/init_db.py

# 6. 启动服务
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

**访问 API 文档**: http://localhost:8000/docs

### Web 前端开发

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 访问 http://localhost:3000
```

---

## 📱 应用截图

> **注意**: 这里可以添加应用截图

- 快递管理界面
- 短信列表界面
- 规则管理界面
- 设置界面

---

## 🔧 核心功能说明

### 短信分类

应用使用规则引擎自动对短信进行分类：

1. **内置规则** - 预置常用分类规则（验证码、银行、快递等）
2. **自定义规则** - 支持通过规则名称、条件、标签等创建自定义规则
3. **规则优先级** - 支持设置规则优先级，高优先级规则优先匹配

**支持的分类类型**:
- 🔐 验证码 - 验证码短信
- 📦 快递 - 快递通知短信
- 🏦 银行 - 银行相关短信
- 🔔 通知 - 系统通知短信
- 📢 营销 - 营销推广短信

### 快递取件码提取

应用能够从短信中智能提取快递取件码：

- **支持的快递平台**: 
  - 菜鸟驿站: `凭X-X-XXXX` 格式
  - 兔喜生活: `00-7956` 等连字符格式
  - 京东物流: 常见格式
  - 其他主流快递平台

- **提取的信息**:
  - ✅ 取件码
  - ✅ 快递公司
  - ✅ 取件地址
  - ✅ 取件日期

### 快递管理功能

- **日期分组** - 按日期自动分组，支持折叠/展开
- **状态标记** - 一键标记已取件，支持批量操作
- **搜索筛选** - 支持按取件码、日期、快递公司搜索
- **状态持久化** - 使用 `SharedPreferences` 持久化状态，重启后保持

### 权限说明

应用需要以下权限：

- 📱 **READ_SMS** - 读取短信内容（必需）
- 📱 **RECEIVE_SMS** - 接收新短信通知（可选）
- 📱 **READ_CONTACTS** - 读取联系人信息（可选）
- 🌐 **INTERNET** - 网络访问（用于 API 调用）
- 💾 **存储权限** - 本地数据存储

---

## 📋 API 文档

后端 API 文档可通过以下地址访问：

- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

### 主要 API 端点

- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/sms` - 获取短信列表
- `GET /api/tags` - 获取标签列表
- `GET /api/rules` - 获取规则列表
- `POST /api/rules` - 创建规则

---

## 🛠️ 开发指南

### 添加新的短信分类规则

1. 在 `RuleManageScreen.kt` 中添加规则配置
2. 在 `backend/app/services/sms_classifier.py` 中实现分类逻辑
3. 测试规则匹配效果

### 添加新的快递平台支持

1. 在 `ExpressExtractor.kt` 的 `expressCompanies` 中添加平台标识
2. 添加对应的取件码提取正则表达式
3. 测试提取效果

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 代码规范

- 遵循项目的代码风格
- 添加必要的注释
- 确保代码通过 lint 检查
- 编写清晰的提交信息

---

## ❓ 常见问题

### Q: 应用无法读取短信？
**A**: 请检查是否已授予应用短信读取权限。可以在设置 > 应用 > SMS Agent > 权限中授予。

### Q: 快递取件码无法识别？
**A**: 请检查短信内容是否包含取件码信息。如果仍未识别，可以在"规则管理"中自定义规则。

### Q: 应用崩溃怎么办？
**A**: 
1. 检查 Android 版本是否满足要求（Android 6.0+）
2. 清除应用数据和缓存
3. 重新安装应用
4. 查看日志文件（如果有）

### Q: 如何备份数据？
**A**: 应用使用 SharedPreferences 存储数据，可以通过 Android 备份功能备份。

### Q: 支持哪些快递平台？
**A**: 目前支持菜鸟驿站、兔喜生活、京东物流等主流平台。更多平台支持正在开发中。

---

## 🗺️ 路线图

- [ ] 支持更多快递平台
- [ ] 添加云同步功能
- [ ] 支持数据导出
- [ ] 添加深色模式
- [ ] 支持多语言
- [ ] 添加统计分析功能
- [ ] 支持自定义主题

---

## 📝 更新日志

查看 [CHANGELOG.md](CHANGELOG.md) 了解详细更新历史

**最新版本**: v1.3 (2025-11-20)
- ✨ 修复已取页面快递卡片状态显示问题
- ✨ 改进搜索输入框显示
- ✨ 优化快递提取规则

---

## 🤝 贡献

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

### 贡献指南

- 遵循项目的代码风格和规范
- 添加必要的注释和文档
- 确保代码通过 lint 检查
- 编写清晰的提交信息和 PR 描述

---

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者和用户！

特别感谢：
- Jetpack Compose 团队
- FastAPI 团队
- Vue.js 团队

---

## 📞 联系方式与支持

- **问题反馈**: [GitHub Issues](https://github.com/your-username/sms_agent/issues)
- **功能建议**: [GitHub Discussions](https://github.com/your-username/sms_agent/discussions)
- **Email**: your-email@example.com

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star！**

**Made with ❤️ by SMS Agent Team**

[⬆ 返回顶部](#-sms-agent---智能短信助手)

</div>


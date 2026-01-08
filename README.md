# 📱 QuickCode - 智能快递取件码助手

<div align="center">

![Version](https://img.shields.io/badge/version-1.4.1-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Platform](https://img.shields.io/badge/platform-Android-lightgrey.svg)
![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-orange.svg)
![Compose](https://img.shields.io/badge/Compose-1.5.0-blue.svg)

**让取件码不再难找的短信助手**

一个专注于「快递取件码识别与管理」的 Android 应用，自动识别短信中的快递通知，提取取件码、站点地址和时间信息。

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术栈](#-技术栈) • [项目结构](#-项目结构) • [贡献指南](#-贡献指南)

</div>

---

## ✨ 功能特性

### 📦 快递管理核心功能

- **🚀 自动提取取件码** - 从短信中智能识别并提取快递取件码，支持多种格式
- **📅 日期分组显示** - 按日期自动分组，支持折叠/展开，快速定位
- **✅ 快速标记** - 一键标记已取件，支持批量操作
- **🔍 搜索筛选** - 支持取件码和日期搜索，快速找到目标快递
- **🏢 多种快递平台支持** - 菜鸟驿站、兔喜生活、京东物流、顺丰、中通、圆通、韵达等主流平台
- **📍 地址提取** - 自动提取快递站点地址信息
- **⏰ 时间识别** - 识别快递到达时间，按时间排序

### 💬 短信管理

- **📋 短信列表** - 清晰展示所有短信，按时间倒序排列
- **🔎 搜索功能** - 支持短信内容搜索
- **🏷️ 智能分类** - 自动分类快递、验证码、银行、通知等类型短信
- **📱 实时接收** - 支持实时接收新短信通知

### 🎨 用户体验

- **✨ 玻璃拟态设计** - 现代化的 UI 设计风格，视觉体验优雅
- **🎭 流畅动画** - 丰富的交互动画效果，操作流畅自然
- **📱 响应式布局** - 适配不同屏幕尺寸，完美支持各种设备
- **🌙 深色模式** - 支持深色主题（规划中）

### 🔓 开源特性

- **✅ 完全开源** - 无激活码、无使用限制、无功能限制
- **🆓 永久免费** - 所有功能完全免费使用
- **🔧 可定制** - 代码完全开放，可根据需求自由定制

---

## 🏗️ 技术栈

### Android 端

- **语言**: Kotlin 1.9.0+
- **UI 框架**: Jetpack Compose 1.5.0+
- **架构模式**: MVVM
- **最小 SDK**: 23 (Android 6.0)
- **目标 SDK**: 34 (Android 14)
- **构建工具**: Gradle 8.0+
- **依赖注入**: 手动依赖注入
- **数据存储**: SharedPreferences, Room (规划中)

### 后端 API（可选）

- **框架**: FastAPI
- **语言**: Python 3.10+
- **数据库**: PostgreSQL / SQLite
- **ORM**: SQLAlchemy 2.0+
- **认证**: JWT

### 前端 Web（可选）

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Vant 4
- **状态管理**: Pinia
- **HTTP 客户端**: Axios

---

## 📁 项目结构

```
QuickCode/
├── android/                          # Android 原生应用（主要）
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/sms/tagger/
│   │   │   │   ├── MainActivity.kt          # 主 Activity
│   │   │   │   ├── SmsAgentApplication.kt    # Application 类
│   │   │   │   ├── data/                    # 数据层
│   │   │   │   │   ├── model/               # 数据模型
│   │   │   │   │   ├── remote/              # 网络请求（Retrofit）
│   │   │   │   │   └── repository/          # 数据仓库
│   │   │   │   ├── ui/                      # UI 层
│   │   │   │   │   ├── screens/             # 页面
│   │   │   │   │   │   ├── ExpressScreen.kt      # 快递管理页面（核心）
│   │   │   │   │   │   ├── ExpressPickedScreen.kt # 已取快递页面
│   │   │   │   │   │   ├── SmsListScreen.kt      # 短信列表页面
│   │   │   │   │   │   ├── SettingsScreen.kt     # 设置页面
│   │   │   │   │   │   ├── RuleManageScreen.kt   # 规则管理页面
│   │   │   │   │   │   └── ...
│   │   │   │   │   ├── components/          # 组件
│   │   │   │   │   │   ├── GlassComponents.kt    # 玻璃拟态组件
│   │   │   │   │   │   ├── LimitDialogs.kt      # 限制提示对话框
│   │   │   │   │   │   └── ...
│   │   │   │   │   ├── theme/                # 主题配置
│   │   │   │   │   └── viewmodel/            # ViewModel
│   │   │   │   └── util/                     # 工具类
│   │   │   │       ├── SmsReader.kt           # 短信读取
│   │   │   │       ├── ExpressExtractor.kt    # 快递信息提取（核心）
│   │   │   │       ├── ExpressCompanyDetector.kt # 快递公司识别
│   │   │   │       ├── StrictExpressFilter.kt   # 严格快递过滤
│   │   │   │       ├── SmsReceiver.kt          # 短信接收器
│   │   │   │       ├── RuleEngine.kt          # 规则引擎
│   │   │   │       ├── NotificationHelper.kt  # 通知助手
│   │   │   │       └── ...
│   │   │   ├── res/                           # 资源文件
│   │   │   │   ├── mipmap-*/                  # 应用图标
│   │   │   │   ├── drawable/                  # 快递公司图标等
│   │   │   │   └── ...
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   ├── build.gradle
│   ├── settings.gradle
│   └── gradle.properties
│
├── backend/                          # 后端 API 服务（可选）
│   ├── app/
│   │   ├── main.py                   # FastAPI 应用入口
│   │   ├── routers/                   # API 路由
│   │   │   ├── auth.py                # 认证相关
│   │   │   ├── sms.py                 # 短信管理
│   │   │   ├── tags.py                # 标签管理
│   │   │   └── extraction_rules.py    # 规则管理
│   │   ├── models/                    # 数据模型
│   │   ├── schemas/                   # Pydantic 模式
│   │   ├── services/                  # 业务逻辑
│   │   │   ├── sms_classifier.py      # 短信分类器
│   │   │   └── rule_engine.py         # 规则引擎
│   │   └── database.py                # 数据库配置
│   └── requirements.txt
│
├── frontend/                         # Web 前端应用（可选）
│   ├── src/
│   │   ├── views/                     # 页面组件
│   │   ├── components/                # 公共组件
│   │   ├── api/                       # API 封装
│   │   └── store/                     # 状态管理
│   └── package.json
│
├── docs/                             # 文档目录
│   ├── 1.新增通知.md
│   ├── 2.无法开启通知.md
│   └── ...
│
├── scripts/                          # 工具脚本
│   ├── init_db.py                     # 数据库初始化
│   └── ...
│
├── index.html                         # 官网首页
├── README.md                          # 项目说明（本文件）
└── .gitignore                        # Git 忽略文件
```

---

## 🚀 快速开始

### 前置要求

#### Android 开发环境

- **JDK**: 8 或更高版本（推荐 JDK 17）
- **Android Studio**: Hedgehog | 2023.1.1+ 或更高版本
- **Android SDK**: API 34 (Android 14)
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+

#### 系统要求

- **Android 设备**: Android 6.0 (API 23) 或更高版本
- **存储空间**: 至少 50MB 可用空间
- **网络**: 可选（用于可选的 API 调用，本地使用不需要）

### Android 应用安装

#### 方式一：从源码构建（推荐）

```bash
# 1. 克隆项目
git clone https://github.com/jencyfei/QuickCode.git
cd QuickCode

# 2. 打开 Android Studio
# 3. 打开 android 目录作为项目
# 4. 等待 Gradle 同步完成
# 5. 连接 Android 设备或启动模拟器
# 6. 点击运行按钮或使用快捷键 Shift+F10

# 或者使用命令行构建 Release APK
cd android
./gradlew assembleRelease  # Linux/Mac
.\gradlew.bat assembleRelease  # Windows

# APK 文件位置: android/app/build/outputs/apk/release/sms-agent-release-1.4.1.apk
```

#### 方式二：直接安装 APK

从 [Releases](https://github.com/jencyfei/QuickCode/releases) 页面下载最新版本的 APK 文件，然后使用 adb 安装：

```bash
adb install sms-agent-release-1.4.1.apk
```

**首次运行前**:
1. ✅ 确保已授予短信读取权限（必需）
2. ✅ 允许应用在后台运行（可选，用于实时接收短信）
3. ✅ 允许应用通知权限（可选，用于新快递通知）

### 后端服务部署（可选）

如果需要使用后端 API 功能：

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

### Web 前端开发（可选）

如果需要使用 Web 前端：

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

## 🔧 核心功能说明

### 快递取件码提取

应用能够从短信中智能提取快递取件码，支持多种格式：

- **支持的快递平台**: 
  - 🏪 菜鸟驿站: `凭X-X-XXXX` 格式
  - 🐰 兔喜生活: `00-7956` 等连字符格式
  - 📦 京东物流: 常见格式
  - 🚚 顺丰速运: SF 格式
  - 📮 中通快递: ZTO 格式
  - 🔵 圆通速递: YTO 格式
  - 🟢 韵达速递: YD 格式
  - 其他主流快递平台

- **提取的信息**:
  - ✅ 取件码
  - ✅ 快递公司
  - ✅ 取件地址
  - ✅ 取件日期
  - ✅ 快递单号（部分）

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

### 快递管理功能

- **日期分组** - 按日期自动分组，支持折叠/展开
- **状态标记** - 一键标记已取件，支持批量操作
- **搜索筛选** - 支持按取件码、日期、快递公司搜索
- **状态持久化** - 使用 `SharedPreferences` 持久化状态，重启后保持
- **未取/已取分类** - 清晰区分未取和已取快递

### 权限说明

应用需要以下权限：

- 📱 **READ_SMS** - 读取短信内容（必需）
- 📱 **RECEIVE_SMS** - 接收新短信通知（可选）
- 📱 **READ_CONTACTS** - 读取联系人信息（可选，用于显示联系人名称）
- 🌐 **INTERNET** - 网络访问（可选，用于可选的 API 调用）
- 🔔 **POST_NOTIFICATIONS** - 通知权限（可选，Android 13+）

---

## 📱 应用截图

<div align="left">

<img src="screenshots/screenshot1.jpg" alt="快递管理界面（未取快递）" width="30%" style="margin-right: 1%;" />
<img src="screenshots/screenshot2.jpg" alt="快递管理界面（已取快递）" width="30%" style="margin-right: 1%;" />
<img src="screenshots/screenshot3.jpg" alt="设置界面" width="30%" />

</div>



---

## 🛠️ 开发指南

### 添加新的快递平台支持

1. 在 `ExpressExtractor.kt` 的 `expressCompanies` 中添加平台标识
2. 添加对应的取件码提取正则表达式
3. 在 `res/drawable/` 中添加快递公司图标
4. 测试提取效果

### 添加新的短信分类规则

1. 在 `RuleManageScreen.kt` 中添加规则配置
2. 在 `RuleEngine.kt` 中实现分类逻辑
3. 测试规则匹配效果

### 自定义 UI 主题

修改 `ui/theme/` 目录下的主题文件：
- `Color.kt` - 颜色定义
- `Theme.kt` - 主题配置
- `Type.kt` - 字体配置

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. **Fork 本项目**
2. **创建特性分支** (`git checkout -b feature/AmazingFeature`)
3. **提交更改** (`git commit -m 'Add some AmazingFeature'`)
4. **推送到分支** (`git push origin feature/AmazingFeature`)
5. **开启 Pull Request**

### 代码规范

- ✅ 遵循项目的代码风格（Kotlin 官方风格指南）
- ✅ 添加必要的注释和文档
- ✅ 确保代码通过 lint 检查
- ✅ 编写清晰的提交信息
- ✅ 为新功能添加测试（如果适用）

### 提交信息规范

使用清晰的提交信息，格式如下：

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型 (type)**:
- `feat`: 新功能
- `fix`: 修复 bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建/工具相关

---

## ❓ 常见问题

### Q: 应用无法读取短信？

**A**: 请检查是否已授予应用短信读取权限。可以在设置 > 应用 > QuickCode > 权限中授予。

### Q: 快递取件码无法识别？

**A**: 
1. 请检查短信内容是否包含取件码信息
2. 如果仍未识别，可以在"规则管理"中自定义规则
3. 查看应用日志了解详细原因

### Q: 应用崩溃怎么办？

**A**: 
1. 检查 Android 版本是否满足要求（Android 6.0+）
2. 清除应用数据和缓存
3. 重新安装应用
4. 查看日志文件或提交 Issue

### Q: 如何备份数据？

**A**: 应用使用 SharedPreferences 存储数据，可以通过 Android 备份功能备份。未来版本将支持数据导出功能。

### Q: 支持哪些快递平台？

**A**: 目前支持菜鸟驿站、兔喜生活、京东物流、顺丰、中通、圆通、韵达等主流平台。更多平台支持正在开发中，也欢迎提交 PR 添加新平台支持。

### Q: 应用会联网吗？

**A**: 应用完全离线运行，不会上传任何数据到服务器。只有在使用可选的 Web 前端或后端 API 功能时才会联网。

---

## 🗺️ 路线图

- [x] 基础快递取件码提取
- [x] 日期分组和状态管理
- [x] 搜索和筛选功能
- [x] 多种快递平台支持
- [x] 开源版本（移除激活限制）
- [ ] 支持更多快递平台
- [ ] 添加数据导出功能
- [ ] 添加深色模式
- [ ] 支持多语言
- [ ] 添加统计分析功能
- [ ] 支持自定义主题
- [ ] 云同步功能（可选）
- [ ] 桌面小组件

---

## 📝 更新日志

查看 [CHANGELOG.md](CHANGELOG.md) 了解详细更新历史

**最新版本**: v1.4.1 (2026-01-05)
- ✨ 开源版本：移除所有激活和使用限制
- ✨ 移除 Trial 版本，统一为完整版
- ✨ 优化快递提取规则
- 🐛 修复已知问题

**历史版本**:
- v1.3 (2025-11-20) - 修复已取页面快递卡片状态显示问题，改进搜索输入框
- v1.2 - 优化快递提取规则，添加更多快递平台支持
- v1.1 - 添加日期分组和状态管理功能
- v1.0 - 初始版本发布

---

## 📄 许可证

本项目采用 **MIT 许可证** - 查看 [LICENSE](LICENSE) 文件了解详情

---

## 🙏 致谢

感谢所有为本项目做出贡献的开发者和用户！

特别感谢：
- 🎨 Jetpack Compose 团队
- 🐍 FastAPI 团队
- ⚡ Vue.js 团队
- 📦 所有开源库的维护者

---

## 🎁 微信小程序

开发者的另一个项目：**智能选礼物助手**，帮助您轻松为他人挑选合适的礼物！

> 💡 **提示**：当前开源项目（QuickCode）是为微信小程序引流的，如果您觉得 QuickCode 对您有帮助，欢迎扫码体验我们的微信小程序！

<div align="center">

<img src="assets/wechat_miniprogram.png" alt="微信小程序二维码" width="200" />

**扫码体验智能选礼物助手小程序**

</div>

---

## 💝 支持项目

如果您觉得这个项目对您有帮助，欢迎通过以下方式支持项目发展：

<div align="center">

### 支付宝捐赠

<img src="assets/alipay_donate.png" alt="支付宝收款码" width="200" />

**感谢您的支持！您的捐赠将用于项目的持续开发和维护。**

</div>

---

## 📞 联系方式与支持

- **问题反馈**: [GitHub Issues](https://github.com/jencyfei/QuickCode/issues)
- **功能建议**: [GitHub Discussions](https://github.com/jencyfei/QuickCode/discussions)
- **项目地址**: https://github.com/jencyfei/QuickCode
- **微信小程序**: 扫码上方二维码体验智能选礼物助手（开发者的另一个项目）

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给个 Star！**

**Made with ❤️ by QuickCode Team**

[⬆ 返回顶部](#-quickcode---智能快递取件码助手)

</div>

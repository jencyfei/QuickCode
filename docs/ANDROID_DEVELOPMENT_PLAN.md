# SMS Agent 安卓应用开发计划

## 📋 项目概述

基于现有的 Web 应用，开发一个原生安卓应用，实现短信的自动分类、标签管理和智能提取功能。

## 🎯 核心功能

### 1. 短信读取与监听

**功能**:
- 读取手机中的所有短信
- 实时监听新收到的短信
- 自动同步到服务器

**技术要点**:
- 使用 `ContentProvider` 读取短信数据库
- 使用 `BroadcastReceiver` 监听新短信
- 需要权限：`READ_SMS`, `RECEIVE_SMS`

### 2. 自动分类与打标签

**功能**:
- 根据短信内容自动分类
- 自动打上对应的标签
- 支持自定义分类规则

**实现方式**:
- 调用后端 API 进行分类
- 或在本地实现分类逻辑（离线模式）

### 3. 短信列表展示

**功能**:
- 按标签筛选短信
- 按时间筛选短信
- 搜索短信内容
- 显示短信详情

**UI组件**:
- RecyclerView 展示列表
- SwipeRefreshLayout 下拉刷新
- SearchView 搜索功能

### 4. 标签管理

**功能**:
- 查看所有标签
- 创建/编辑/删除标签
- 查看每个标签的短信数量
- 自定义标签颜色和图标

### 5. 快递取件码提取

**功能**:
- 自动识别快递短信
- 提取取件码
- 一键复制取件码
- 显示快递公司和地点

### 6. 验证码提取

**功能**:
- 自动识别验证码短信
- 提取验证码
- 一键复制验证码
- 支持自动填充（Android 8.0+）

## 🏗️ 技术架构

### 1. 开发语言

**推荐**: Kotlin
- 现代化、简洁
- Google 官方推荐
- 协程支持异步操作

**备选**: Java
- 成熟稳定
- 资料丰富

### 2. 架构模式

**MVVM (Model-View-ViewModel)**

```
┌─────────────┐
│    View     │ (Activity/Fragment)
│  (UI Layer) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  ViewModel  │ (业务逻辑)
└──────┬──────┘
       │
       ↓
┌─────────────┐
│    Model    │ (数据层)
│ Repository  │
└──────┬──────┘
       │
       ├─→ Remote (API)
       └─→ Local (Database)
```

### 3. 核心库

#### UI 框架
- **Jetpack Compose** (推荐) - 现代化声明式UI
- 或 **传统 XML + ViewBinding**

#### 网络请求
- **Retrofit** - REST API 调用
- **OkHttp** - HTTP 客户端
- **Gson** / **Moshi** - JSON 解析

#### 数据库
- **Room** - SQLite ORM
- 用于本地缓存短信数据

#### 异步处理
- **Kotlin Coroutines** - 协程
- **Flow** - 响应式数据流

#### 依赖注入
- **Hilt** - 依赖注入框架

#### 其他
- **Jetpack Navigation** - 页面导航
- **DataStore** - 数据存储
- **WorkManager** - 后台任务

## 📱 界面设计

### 1. 主界面 (MainActivity)

**底部导航栏**:
```
┌─────────────────────────────┐
│                             │
│      内容区域                │
│                             │
│                             │
└─────────────────────────────┘
┌──────┬──────┬──────┬──────┐
│ 短信 │ 标签 │ 快递 │ 我的 │
└──────┴──────┴──────┴──────┘
```

### 2. 短信列表页面

```
┌─────────────────────────────┐
│ ← 短信列表        🔍 ⋮      │
├─────────────────────────────┤
│ 筛选: [全部▼] [今天▼]       │
├─────────────────────────────┤
│ 📱 95533                    │
│ 【建设银行】您的验证码...    │
│ 🏷️ 验证码  2小时前          │
├─────────────────────────────┤
│ 📦 菜鸟驿站                  │
│ 您的快递已到达，取件码...    │
│ 🏷️ 快递  3小时前            │
├─────────────────────────────┤
│ ...                         │
└─────────────────────────────┘
```

### 3. 标签管理页面

```
┌─────────────────────────────┐
│ ← 标签管理        + 新建     │
├─────────────────────────────┤
│ ┌─────────────────────────┐ │
│ │ 🔐 验证码            15 │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 📦 快递              8  │ │
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │ 🏦 银行              12 │ │
│ └─────────────────────────┘ │
└─────────────────────────────┘
```

### 4. 快递页面

```
┌─────────────────────────────┐
│ ← 快递取件                   │
├─────────────────────────────┤
│ 📦 菜鸟驿站                  │
│ 取件码: 1234                │
│ [复制取件码]                │
│ 地址: 小区门口               │
│ 3小时前                     │
├─────────────────────────────┤
│ 📦 丰巢快递柜                │
│ 取件码: 567890              │
│ [复制取件码]                │
│ 地址: 1号楼                 │
│ 昨天                        │
└─────────────────────────────┘
```

## 🔐 权限管理

### 必需权限

```xml
<!-- 读取短信 -->
<uses-permission android:name="android.permission.READ_SMS" />

<!-- 接收短信 -->
<uses-permission android:name="android.permission.RECEIVE_SMS" />

<!-- 网络访问 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 可选权限

```xml
<!-- 读取联系人（用于显示发件人姓名） -->
<uses-permission android:name="android.permission.READ_CONTACTS" />

<!-- 通知权限（Android 13+） -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### 权限请求流程

1. 检查权限是否已授予
2. 如果未授予，显示权限说明
3. 请求权限
4. 处理用户响应

## 📡 API 集成

### 1. API 配置

**Base URL**: `http://your-server:10043/api`

**认证方式**: JWT Token

### 2. 主要接口

#### 短信同步
```
POST /sms/sync
Body: {
  "messages": [
    {
      "sender": "95533",
      "content": "...",
      "received_at": "2025-11-10T10:00:00Z"
    }
  ]
}
```

#### 获取标签
```
GET /tags
Response: [
  {
    "id": 1,
    "name": "验证码",
    "color": "#FF6B9D",
    "icon": "🔐"
  }
]
```

#### 短信分类
```
POST /sms/classify
Body: {
  "content": "您的验证码是123456",
  "sender": "95533"
}
Response: {
  "tags": ["验证码"]
}
```

## 🗄️ 本地数据库设计

### 表结构

#### SMS 表
```kotlin
@Entity(tableName = "sms_messages")
data class SmsMessage(
    @PrimaryKey val id: Long,
    val sender: String,
    val content: String,
    val receivedAt: Long,
    val tagIds: String, // JSON 数组
    val synced: Boolean = false
)
```

#### Tag 表
```kotlin
@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey val id: Int,
    val name: String,
    val color: String,
    val icon: String
)
```

## 🔄 数据同步策略

### 1. 初始同步

- 首次启动时读取所有短信
- 批量上传到服务器
- 显示同步进度

### 2. 增量同步

- 监听新短信
- 实时上传到服务器
- 自动分类并打标签

### 3. 离线模式

- 本地缓存短信数据
- 离线时使用本地分类规则
- 联网后自动同步

## 📦 项目结构

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/smsagent/
│   │   │   ├── data/
│   │   │   │   ├── local/        # 本地数据库
│   │   │   │   ├── remote/       # API 接口
│   │   │   │   └── repository/   # 数据仓库
│   │   │   ├── domain/
│   │   │   │   ├── model/        # 数据模型
│   │   │   │   └── usecase/      # 业务逻辑
│   │   │   ├── presentation/
│   │   │   │   ├── sms/          # 短信列表
│   │   │   │   ├── tag/          # 标签管理
│   │   │   │   ├── express/      # 快递页面
│   │   │   │   └── profile/      # 个人中心
│   │   │   ├── service/          # 后台服务
│   │   │   ├── receiver/         # 广播接收器
│   │   │   └── util/             # 工具类
│   │   ├── res/
│   │   │   ├── layout/           # 布局文件
│   │   │   ├── drawable/         # 图片资源
│   │   │   ├── values/           # 配置文件
│   │   │   └── navigation/       # 导航图
│   │   └── AndroidManifest.xml
│   └── test/                     # 单元测试
└── build.gradle
```

## 🚀 开发步骤

### 阶段1: 项目搭建 (1-2天)

- [ ] 创建 Android 项目
- [ ] 配置依赖库
- [ ] 设置项目结构
- [ ] 配置 API 连接

### 阶段2: 核心功能 (3-5天)

- [ ] 实现短信读取
- [ ] 实现短信监听
- [ ] 实现数据同步
- [ ] 实现本地数据库

### 阶段3: UI 开发 (3-5天)

- [ ] 短信列表页面
- [ ] 标签管理页面
- [ ] 快递页面
- [ ] 个人中心页面

### 阶段4: 高级功能 (2-3天)

- [ ] 自动分类
- [ ] 验证码提取
- [ ] 快递码提取
- [ ] 搜索功能

### 阶段5: 优化与测试 (2-3天)

- [ ] 性能优化
- [ ] UI/UX 优化
- [ ] 单元测试
- [ ] 集成测试

## 📚 参考资源

### 官方文档
- [Android Developers](https://developer.android.com/)
- [Kotlin 文档](https://kotlinlang.org/docs/home.html)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)

### 开源项目
- [QKSMS](https://github.com/moezbhatti/qksms) - 开源短信应用
- [Simple SMS Messenger](https://github.com/SimpleMobileTools/Simple-SMS-Messenger)

### 学习资源
- [Android Basics in Kotlin](https://developer.android.com/courses/android-basics-kotlin/course)
- [Jetpack Compose Tutorial](https://developer.android.com/jetpack/compose/tutorial)

## 💡 开发建议

### 1. 从简单开始

先实现基本的短信读取和展示，再逐步添加高级功能。

### 2. 使用现代技术栈

- Kotlin + Coroutines
- Jetpack Compose
- MVVM 架构

### 3. 注重用户体验

- 流畅的动画
- 直观的操作
- 清晰的反馈

### 4. 考虑性能

- 分页加载
- 图片缓存
- 后台同步

### 5. 安全性

- 加密敏感数据
- 安全的网络通信
- 权限最小化

## 🎯 下一步行动

1. **确定技术栈**
   - Kotlin 还是 Java？
   - Jetpack Compose 还是传统 XML？

2. **创建项目**
   - 使用 Android Studio 创建新项目
   - 配置 Gradle 依赖

3. **实现 MVP**
   - 先实现最小可行产品
   - 短信读取 + 列表展示

4. **迭代开发**
   - 逐步添加功能
   - 持续测试和优化

---

**准备开始**: ✅ Web 应用已稳定  
**下一步**: 创建 Android 项目  
**预计时间**: 2-3周完成基本功能  
**技术栈**: Kotlin + Jetpack Compose + MVVM

# 导入功能自动隐藏方案

## 📅 创建日期
2025年11月6日

## 🎯 需求背景

### Web环境
- 用户需要手动粘贴短信内容导入
- "导入短信"功能是必需的

### App环境
- App可以通过系统权限直接读取短信
- 不需要手动粘贴
- "导入"入口可以隐藏，简化界面

---

## 💡 解决方案

### 方案设计思路

1. **环境自动检测**
   - 检测当前运行环境（Web/Android/iOS）
   - App环境自动隐藏导入功能
   - Web环境保留导入功能

2. **用户可配置**
   - 在设置中提供开关
   - 允许用户手动控制显示/隐藏
   - 高级用户可在App中临时启用

3. **智能优先级**
   - 用户设置 > 自动检测
   - 保证最大灵活性

---

## 🛠️ 技术实现

### 1. 环境检测工具（environment.js）

```javascript
// 检测方法1: User-Agent
const ua = navigator.userAgent.toLowerCase()
if (ua.includes('sms-tagger-app')) {
  return true  // 在App中
}

// 检测方法2: 原生API对象
if (window.SmsNativeAPI) {
  return true  // Android App注入的对象
}

if (window.webkit?.messageHandlers) {
  return true  // iOS App注入的对象
}

// 检测方法3: URL参数（测试用）
const urlParams = new URLSearchParams(window.location.search)
if (urlParams.get('platform') === 'app') {
  return true
}
```

---

### 2. 显示逻辑

```javascript
function shouldShowImportFeature() {
  // 1. 优先检查用户设置
  const userSetting = localStorage.getItem('show_import_feature')
  if (userSetting !== null) {
    return userSetting === 'true'
  }
  
  // 2. 根据环境自动判断
  return !isInApp()  // Web显示，App隐藏
}
```

---

### 3. 条件渲染

```vue
<!-- 底部导航栏 -->
<van-tabbar>
  <van-tabbar-item to="/express-detail">快递</van-tabbar-item>
  <van-tabbar-item to="/tag-manage">标签</van-tabbar-item>
  
  <!-- 条件显示导入功能 -->
  <van-tabbar-item 
    v-if="showImport" 
    to="/sms-import"
  >
    导入
  </van-tabbar-item>
  
  <van-tabbar-item to="/settings">设置</van-tabbar-item>
</van-tabbar>

<script setup>
import { shouldShowImportFeature } from '@/utils/environment'

const showImport = ref(shouldShowImportFeature())
</script>
```

---

## 📱 App集成指南

### Android集成

#### 1. 设置User-Agent
```kotlin
// MainActivity.kt
webView.settings.userAgentString = 
  "${webView.settings.userAgentString} SmsT agger-App"
```

#### 2. 注入原生API对象
```kotlin
class SmsNativeAPI(private val context: Context) {
    @JavascriptInterface
    fun readAllSms(): String {
        // 读取系统短信
        return smsListJson
    }
}

// 注入到WebView
webView.addJavascriptInterface(
    SmsNativeAPI(this), 
    "SmsNativeAPI"
)
```

---

### iOS集成

#### 1. 设置User-Agent
```swift
// ViewController.swift
let userAgent = webView.value(forKey: "userAgent") as? String
webView.customUserAgent = "\(userAgent ?? "") SmsTag ger-App"
```

#### 2. 注入原生API
```swift
// 配置WKWebView
let config = WKWebViewConfiguration()
config.userContentController.add(self, name: "smsNative")

// 处理消息
func userContentController(
    _ userContentController: WKUserContentController, 
    didReceive message: WKScriptMessage
) {
    if message.name == "smsNative" {
        // 读取短信并返回
    }
}
```

---

## 🧪 测试方案

### 1. Web环境测试
```bash
# 直接访问
http://localhost:3000/

# 预期结果
✅ 底部导航显示"导入"按钮
✅ 可以访问 /sms-import 页面
```

---

### 2. App环境模拟测试（开发阶段）
```bash
# 方法1: 添加URL参数
http://localhost:3000/?platform=app

# 方法2: 修改浏览器User-Agent
# Chrome DevTools > Network Conditions > User Agent
# 添加: SmsTagg er-App

# 预期结果
✅ 底部导航隐藏"导入"按钮
✅ 只显示3个按钮：快递、标签、设置
```

---

### 3. 用户设置测试
```javascript
// 在浏览器控制台执行

// 强制显示导入功能
localStorage.setItem('show_import_feature', 'true')
location.reload()

// 强制隐藏导入功能
localStorage.setItem('show_import_feature', 'false')
location.reload()

// 恢复自动检测
localStorage.removeItem('show_import_feature')
location.reload()
```

---

## ⚙️ 设置页面集成（可选）

### 添加开关控制

```vue
<!-- Settings.vue -->
<template>
  <div class="settings-page">
    <!-- 高级设置 -->
    <van-cell-group title="高级设置">
      <van-cell title="显示导入功能">
        <template #right-icon>
          <van-switch 
            v-model="showImportFeature"
            @change="onImportFeatureChange"
          />
        </template>
      </van-cell>
      <van-cell>
        <template #title>
          <div class="setting-desc">
            在App环境下默认隐藏，Web环境下默认显示
          </div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  shouldShowImportFeature, 
  setShowImportFeature 
} from '@/utils/environment'

const showImportFeature = ref(shouldShowImportFeature())

const onImportFeatureChange = (value) => {
  setShowImportFeature(value)
  // 提示用户需要刷新
  showToast('设置将在刷新后生效')
}
</script>
```

---

## 📊 API设计（App原生功能）

### 1. 读取所有短信
```javascript
// JavaScript调用原生API
async function readAllSms() {
  if (window.SmsNativeAPI) {
    // Android
    const smsJson = window.SmsNativeAPI.readAllSms()
    return JSON.parse(smsJson)
  }
  
  if (window.webkit?.messageHandlers?.smsNative) {
    // iOS
    return new Promise((resolve) => {
      window.webkit.messageHandlers.smsNative.postMessage({
        action: 'readAllSms'
      })
      
      // 监听回调
      window.onSmsDataReceived = (data) => {
        resolve(data)
      }
    })
  }
  
  throw new Error('Native API not available')
}
```

---

### 2. 数据格式
```javascript
// 原生API返回的数据格式
[
  {
    id: "1",
    sender: "95533",
    content: "【验证码】您的验证码是123456",
    received_at: "2025-11-06T10:30:00",
    phone_number: "95533"
  },
  {
    id: "2",
    sender: "菜鸟驿站",
    content: "您的快递已到达，取件码：1234",
    received_at: "2025-11-05T15:20:00",
    phone_number: "10086"
  }
]
```

---

### 3. 权限处理
```kotlin
// Android - 请求短信权限
private fun requestSmsPermission() {
    if (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_SMS),
            SMS_PERMISSION_CODE
        )
    }
}

// 读取短信
fun readAllSms(): String {
    val smsList = mutableListOf<SmsData>()
    val cursor = contentResolver.query(
        Uri.parse("content://sms/inbox"),
        arrayOf("address", "body", "date"),
        null,
        null,
        "date DESC"
    )
    
    cursor?.use {
        while (it.moveToNext()) {
            val sms = SmsData(
                id = it.getLong(it.getColumnIndex("_id")).toString(),
                sender = it.getString(it.getColumnIndex("address")),
                content = it.getString(it.getColumnIndex("body")),
                received_at = Date(it.getLong(it.getColumnIndex("date")))
            )
            smsList.add(sms)
        }
    }
    
    return Gson().toJson(smsList)
}
```

---

## 🔄 更新流程

### 当前状态（已实现）
- ✅ 创建环境检测工具
- ✅ 修改快递详情页（ExpressDetail.vue）
- ✅ 支持条件显示导入功能

### 待完成
- ⏸️ 修改其他页面的底部导航栏（标签、设置等）
- ⏸️ 添加设置页面的开关控制
- ⏸️ App原生集成（Android/iOS）

---

## 📝 修改清单

### 需要修改的文件

#### 1. 已修改
- ✅ `frontend/src/utils/environment.js` - 环境检测工具（新建）
- ✅ `frontend/src/views/ExpressDetail.vue` - 条件显示导入

#### 2. 需要同步修改
- ⏸️ `frontend/src/views/TagManageNew.vue`
- ⏸️ `frontend/src/views/SmsListNew.vue`
- ⏸️ `frontend/src/views/SmsImport.vue`
- ⏸️ `frontend/src/views/Settings.vue`

---

## 💡 使用示例

### 场景1：Web环境（默认）
```
用户打开浏览器访问
  ↓
自动检测环境 = Web
  ↓
showImport = true
  ↓
底部导航显示：[快递] [标签] [导入] [设置]
```

---

### 场景2：App环境（自动隐藏）
```
用户打开App
  ↓
检测到 User-Agent 包含 "SmsTag ger-App"
  ↓
showImport = false
  ↓
底部导航显示：[快递] [标签] [设置]
```

---

### 场景3：App环境 + 用户强制启用
```
用户在设置中打开"显示导入功能"
  ↓
localStorage.setItem('show_import_feature', 'true')
  ↓
showImport = true（优先级高于自动检测）
  ↓
底部导航显示：[快递] [标签] [导入] [设置]
```

---

## 📊 总结

### 核心优势
1. ✅ **自动化**：根据环境自动显示/隐藏
2. ✅ **灵活性**：用户可在设置中覆盖
3. ✅ **兼容性**：支持Web、Android、iOS
4. ✅ **可测试**：提供多种测试方法

### 实现状态
- **Phase 1**：基础框架 ✅ 已完成
- **Phase 2**：全页面应用 ⏸️ 进行中
- **Phase 3**：App集成 ⏸️ 待开始

---

🎉 **基础框架已完成，可以开始测试Web环境和App环境的切换了！**


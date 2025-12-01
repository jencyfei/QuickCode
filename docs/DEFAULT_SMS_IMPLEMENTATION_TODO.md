# 默认短信应用改造 - TODO清单

## 📋 概述

根据 `docs/改造默认短信.md` 需求文档，需要实现成为系统默认短信应用的最小能力，以解决小米14等设备上无法读取所有短信的问题。

---

## ✅ 当前已有功能

1. **SmsReceiver** - 已存在，但监听的是 `SMS_RECEIVED`（普通接收）
2. **SmsReader** - 可以读取短信（`content://sms`）
3. **权限声明** - 已有 `READ_SMS`, `RECEIVE_SMS`
4. **MainActivity** - 已有主Activity和多个Screen

---

## ❌ 缺失功能清单

### 阶段1：权限和基础配置

#### TODO-1: 权限配置 ⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/AndroidManifest.xml`

**任务**:
- [ ] 添加 `SEND_SMS` 权限
- [ ] 添加 `WRITE_SMS` 权限

**代码示例**:
```xml
<uses-permission android:name="android.permission.SEND_SMS" />
<uses-permission android:name="android.permission.WRITE_SMS" />
```

---

### 阶段2：短信接收模块（核心）

#### TODO-2: SMS_DELIVER BroadcastReceiver ⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/util/SmsReceiver.kt`

**当前状态**: 
- ✅ 已有 `SMS_RECEIVED` 监听（普通接收）
- ❌ 缺少 `SMS_DELIVER` 监听（默认短信应用专用）

**任务**:
- [ ] 添加 `SMS_DELIVER` Action的监听
- [ ] 在Manifest中注册 `SMS_DELIVER` Intent Filter
- [ ] 需要权限：`android.permission.BROADCAST_SMS`（已有）

**Manifest配置**:
```xml
<receiver
    android:name=".util.SmsReceiver"
    android:exported="true"
    android:permission="android.permission.BROADCAST_SMS">
    <!-- 现有：普通短信接收 -->
    <intent-filter android:priority="999">
        <action android:name="android.provider.Telephony.SMS_RECEIVED" />
    </intent-filter>
    <!-- 新增：默认短信应用专用 -->
    <intent-filter>
        <action android:name="android.provider.Telephony.SMS_DELIVER_ACTION" />
    </intent-filter>
</receiver>
```

**代码修改**:
```kotlin
override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action
    when (action) {
        Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
            // 现有的普通接收逻辑
        }
        Telephony.Sms.Intents.SMS_DELIVER_ACTION -> {
            // 新增：默认短信应用专用接收逻辑
            handleSmsDeliver(context, intent)
        }
    }
}
```

---

#### TODO-8: 短信接收后处理增强 ⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/util/SmsReceiver.kt`

**任务**:
- [ ] 收到短信后执行取件码识别
- [ ] 将短信数据写入App自己的数据库
- [ ] 触发UI更新（如果App在前台）

**需要调用**:
- `ExpressExtractor` - 取件码识别
- `SmsRepository` - 保存到本地数据库

---

### 阶段3：短信发送和写入能力（Stub）

#### TODO-3: 短信发送Service（Stub）⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/util/SmsSendService.kt` (新建)

**任务**:
- [ ] 创建 `SmsSendService` 类
- [ ] 实现发送短信的最简方法（使用 `SmsManager`）
- [ ] 不提供UI入口
- [ ] 仅满足系统检查需求

**代码结构**:
```kotlin
class SmsSendService {
    fun sendSms(phoneNumber: String, message: String) {
        // Stub实现，满足系统检查
        // 实际不需要真正发送
    }
}
```

**Manifest配置**:
```xml
<service
    android:name=".util.SmsSendService"
    android:exported="true"
    android:permission="android.permission.SEND_SMS">
    <intent-filter>
        <action android:name="android.intent.action.SENDTO" />
        <data android:scheme="sms" />
        <data android:scheme="smsto" />
        <data android:scheme="mms" />
        <data android:scheme="mmsto" />
    </intent-filter>
</service>
```

---

#### TODO-4: 短信写入能力（Stub）⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/util/SmsWriteService.kt` 或工具类 (新建)

**任务**:
- [ ] 创建写入/删除短信的最简实现
- [ ] 提供写入短信到 `content://sms` 的方法
- [ ] 提供删除短信的方法
- [ ] 不在UI中公开入口

**代码结构**:
```kotlin
class SmsWriteService(private val context: Context) {
    fun writeSms(...) {
        // Stub实现，满足系统检查
    }
    
    fun deleteSms(...) {
        // Stub实现，满足系统检查
    }
}
```

---

### 阶段4：默认短信应用切换流程

#### TODO-5: 默认短信应用引导UI ⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/DefaultSmsGuideScreen.kt` (新建)

**任务**:
- [ ] 创建引导用户设置为默认短信应用的Screen
- [ ] 显示说明文案（重点强调"用于识别快递取件码"）
- [ ] 调用系统Intent切换默认短信应用
- [ ] 检查当前是否为默认短信应用

**UI文案**:
```
为了让【XX App】自动识别您的快递取件码，需要您将本应用设为系统短信服务。
不会影响您继续使用微信 / QQ 收发消息，也不会修改您的短信内容。
```

**代码示例**:
```kotlin
val intent = Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT)
intent.putExtra(
    Telephony.Sms.Intents.EXTRA_PACKAGE_NAME,
    context.packageName
)
startActivity(intent)
```

**集成位置**:
- 在 `SettingsScreen` 中添加入口
- 或在首次启动时显示引导

---

#### TODO-9: 检查默认短信应用状态 ⭐⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/util/SmsDefaultAppChecker.kt` (新建)

**任务**:
- [ ] 创建工具类，检查当前是否为默认短信应用
- [ ] 提供方法供UI调用

**代码示例**:
```kotlin
object SmsDefaultAppChecker {
    fun isDefaultSmsApp(context: Context): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }
}
```

---

#### TODO-10: 恢复系统短信应用入口 ⭐⭐⭐
**文件**: `android/app/src/main/java/com/sms/tagger/ui/screens/SettingsScreen.kt`

**任务**:
- [ ] 在设置页面添加"恢复系统短信应用"入口
- [ ] 调用系统Intent让用户选择其他短信应用
- [ ] 作为风险缓解措施

---

### 阶段5：Manifest配置

#### TODO-7: Manifest完整配置 ⭐⭐⭐⭐⭐
**文件**: `android/app/src/main/AndroidManifest.xml`

**任务**:
- [ ] 添加所有必需的权限（TODO-1）
- [ ] 配置SMS_DELIVER Intent Filter（TODO-2）
- [ ] 注册SmsSendService（TODO-3）
- [ ] 配置MainActivity支持默认短信应用（TODO-6）
- [ ] 添加必要的Intent Filter和Category

**MainActivity配置示例**:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <!-- 现有：Launcher -->
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
    <!-- 新增：默认短信应用入口 -->
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <action android:name="android.intent.action.SENDTO" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="sms" />
        <data android:scheme="smsto" />
        <data android:scheme="mms" />
        <data android:scheme="mmsto" />
    </intent-filter>
</activity>
```

---

### 阶段6：短信主页面配置

#### TODO-6: MainActivity作为短信主入口 ⭐⭐⭐⭐⭐
**文件**: 
- `android/app/src/main/AndroidManifest.xml`
- `android/app/src/main/java/com/sms/tagger/MainActivity.kt` (可选修改)

**任务**:
- [ ] 在Manifest中添加默认短信应用的Intent Filter（见TODO-7）
- [ ] 确保MainActivity可以正常打开（已有）
- [ ] 可选：在主页面显示"短信服务已启用"提示

**说明**:
- MainActivity已存在，只需要添加Intent Filter配置
- 不需要大幅修改UI，保持现有轻量体验

---

## 📊 优先级说明

| 优先级 | 说明 |
|--------|------|
| ⭐⭐⭐⭐⭐ | 核心功能，必须实现 |
| ⭐⭐⭐⭐ | 重要功能，建议实现 |
| ⭐⭐⭐ | 辅助功能，可选实现 |

---

## 🔄 实施顺序建议

### 第一阶段：核心配置（必须先完成）
1. **TODO-1**: 权限配置
2. **TODO-7**: Manifest完整配置
3. **TODO-2**: SMS_DELIVER BroadcastReceiver

### 第二阶段：Stub功能（满足系统检查）
4. **TODO-3**: 短信发送Service
5. **TODO-4**: 短信写入能力

### 第三阶段：用户引导和体验
6. **TODO-9**: 检查默认短信应用状态
7. **TODO-5**: 默认短信应用引导UI
8. **TODO-6**: MainActivity配置

### 第四阶段：增强功能
9. **TODO-8**: 短信接收后处理增强
10. **TODO-10**: 恢复系统短信应用入口

---

## ✅ 验收标准

### 功能验收
- [ ] 可以设置为默认短信应用
- [ ] 设置为默认后能读取所有短信（包括10684等）
- [ ] 能接收新短信（SMS_DELIVER）
- [ ] 系统检查通过（发送、写入能力）

### 兼容性验收
- [ ] 小米14（HyperOS / Android 14）测试通过
- [ ] 小米12/13（MIUI 14）测试通过
- [ ] 华为（HarmonyOS 3/4）测试通过
- [ ] OPPO/Vivo（Android 13-14）测试通过

### 体验验收
- [ ] 引导流程清晰易懂
- [ ] 不影响现有功能
- [ ] 保持应用轻量化

---

## 📝 注意事项

1. **最小化实现**: 本次改造只实现最小功能，不添加完整短信应用UI
2. **用户体验**: 引导文案要强调"不会影响微信使用"、"不会修改短信"
3. **兼容性**: 需要在不同品牌的设备上测试
4. **权限处理**: 动态权限请求需要用户手动授权

---

## 🔗 相关文档

- 需求文档: `docs/改造默认短信.md`
- 根因分析: `docs/ROOT_CAUSE_ANALYSIS_10684.md`


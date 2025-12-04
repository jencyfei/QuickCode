# 默认短信应用列表中没有本应用 - 问题分析和修复

## 问题描述

用户反馈：在选择"默认短信应用"时，没有找到本程序的app。

从用户提供的截图可以看到：
- 系统设置页面已成功打开
- 列表中显示了"百度网盘"、"短信"（系统默认）、"换机助手"
- 但没有显示"QuickCode"（本应用）

## 问题分析

### 可能的原因

1. **缺少必要的权限或配置**
   - 某些权限可能缺失
   - Intent Filter 配置不完整
   - 缺少某些系统要求的能力声明

2. **应用未满足系统要求**
   - 系统检查时发现应用缺少某些必需组件
   - Service 或 Receiver 配置不正确

3. **应用安装后未重新扫描**
   - 某些设备需要在安装后重启
   - 或者应用需要至少运行一次才能被系统识别

4. **Android版本或厂商ROM限制**
   - 某些厂商ROM可能有额外的要求
   - Android 10+ 可能有额外的category要求

## 已实施的修复

### 1. 添加缺失的权限

```xml
<!-- 新增权限 -->
<uses-permission android:name="android.permission.RECEIVE_MMS" />
<uses-permission android:name="android.permission.RECEIVE_WAP_PUSH" />
<uses-permission android:name="android.permission.SEND_RESPOND_VIA_MESSAGE" />
```

### 2. 完善 Intent Filter 配置

在 MainActivity 中添加了：
- `android.intent.action.SEND` action（之前只有 SENDTO）
- `android.intent.category.APP_MESSAGING` category（某些系统要求）

### 3. 添加 MMS 接收器

```xml
<receiver
    android:name=".util.SmsReceiver"
    android:exported="true"
    android:permission="android.permission.BROADCAST_WAP_PUSH">
    <intent-filter>
        <action android:name="android.provider.Telephony.WAP_PUSH_DELIVER_ACTION" />
        <data android:mimeType="application/vnd.wap.mms-message" />
    </intent-filter>
</receiver>
```

### 4. 添加快速回复服务

```xml
<service
    android:name=".util.SmsSendService"
    android:exported="true"
    android:permission="android.permission.SEND_RESPOND_VIA_MESSAGE">
    <intent-filter>
        <action android:name="android.intent.action.RESPOND_VIA_MESSAGE" />
        ...
    </intent-filter>
</service>
```

### 5. 更新 SmsSendService 支持快速回复

添加了 `handleRespondViaMessage()` 方法来处理快速回复请求。

## 完整的 Manifest 配置清单

### ✅ 必需的权限
- [x] `READ_SMS`
- [x] `RECEIVE_SMS`
- [x] `SEND_SMS`
- [x] `WRITE_SMS`
- [x] `RECEIVE_MMS`（新增）
- [x] `RECEIVE_WAP_PUSH`（新增）
- [x] `SEND_RESPOND_VIA_MESSAGE`（新增）
- [x] `READ_CONTACTS`

### ✅ 必需的组件

1. **MainActivity Intent Filter**
   - [x] `ACTION_VIEW`
   - [x] `ACTION_SEND`
   - [x] `ACTION_SENDTO`
   - [x] `CATEGORY_DEFAULT`
   - [x] `CATEGORY_BROWSABLE`
   - [x] `CATEGORY_APP_MESSAGING`（新增）
   - [x] 数据scheme: `sms`, `smsto`, `mms`, `mmsto`

2. **BroadcastReceiver (SmsReceiver)**
   - [x] `SMS_RECEIVED` (普通接收)
   - [x] `SMS_DELIVER_ACTION` (默认应用专用)
   - [x] `WAP_PUSH_DELIVER_ACTION` (MMS接收)（新增）

3. **Service (SmsSendService)**
   - [x] `ACTION_SENDTO` (发送短信)
   - [x] `ACTION_RESPOND_VIA_MESSAGE` (快速回复)（新增）

## 测试步骤

### 1. 重新安装应用

1. 卸载旧版本的应用
2. 安装新版本的 APK
3. **启动应用至少一次**（重要！）
4. 授予所有必要的权限

### 2. 检查应用是否出现在列表中

1. 打开系统设置
2. 找到"应用" → "默认应用" → "短信"
3. 查看列表中是否有"QuickCode"

### 3. 如果仍然没有出现

#### 方法1：重启设备
某些设备需要在应用安装后重启才能识别。

#### 方法2：使用 adb 命令检查

```bash
# 检查应用是否被识别为可以处理短信
adb shell dumpsys package com.sms.tagger | grep -i "sms\|mms"

# 检查应用的 intent filters
adb shell dumpsys package com.sms.tagger | grep -A 20 "Activity"

# 检查系统是否识别应用为短信应用
adb shell cmd role list-apps-eligible-for-role SMS
```

#### 方法3：手动触发系统扫描

```bash
# 清除系统缓存
adb shell pm clear com.android.providers.settings

# 重启 Package Manager
adb shell stop && adb shell start
```

### 4. 检查应用配置

使用以下命令验证应用的配置：

```bash
# 检查应用的权限
adb shell dumpsys package com.sms.tagger | grep permission

# 检查应用的组件
adb shell dumpsys package com.sms.tagger | grep -E "Activity|Service|Receiver"

# 检查应用的 Intent Filter
adb shell cmd package query-activities -a android.intent.action.SENDTO
adb shell cmd package query-activities -a android.intent.action.SENDTO | grep com.sms.tagger
```

## 已知问题

1. **某些厂商ROM可能有限制**
   - 华为/荣耀：可能需要额外的权限或配置
   - 小米：可能需要用户在安全中心中授权
   - OPPO/Vivo：可能需要用户在应用管理中启用

2. **应用必须至少运行一次**
   - 系统在应用首次运行时才会扫描和注册组件
   - 安装后必须至少启动应用一次

3. **可能需要重启设备**
   - 某些设备需要重启才能识别新安装的短信应用

## 修改的文件

1. ✅ `android/app/src/main/AndroidManifest.xml`
   - 添加了 `RECEIVE_MMS` 权限
   - 添加了 `RECEIVE_WAP_PUSH` 权限
   - 添加了 `SEND_RESPOND_VIA_MESSAGE` 权限
   - 添加了 `APP_MESSAGING` category
   - 添加了 `ACTION_SEND` action
   - 添加了 MMS 接收器
   - 添加了快速回复服务

2. ✅ `android/app/src/main/java/com/sms/tagger/util/SmsSendService.kt`
   - 添加了 `handleRespondViaMessage()` 方法
   - 添加了 AppLogger 导入

## 下一步

1. **重新打包 APK**（已完成修复）
2. **卸载旧版本并安装新版本**
3. **启动应用至少一次**
4. **授予所有权限**
5. **检查默认短信应用列表**
6. **如果仍然没有，尝试重启设备**

## 版本信息

- 修复版本：1.4.0
- 修复日期：2025-12-02


# 默认短信应用列表中没有本应用 - 修复完成

## 问题描述

用户反馈：在选择"默认短信应用"时，没有找到本程序的app。

从截图可以看到，系统设置页面已成功打开，列表中显示了其他应用，但没有显示"QuickCode"。

## 根本原因

应用缺少成为默认短信应用所需的一些必要权限和组件声明，导致系统未将其识别为可以处理短信的应用。

## 修复内容

### 1. 添加缺失的权限 ⭐⭐⭐⭐⭐

在 `AndroidManifest.xml` 中添加了：
```xml
<uses-permission android:name="android.permission.RECEIVE_MMS" />
<uses-permission android:name="android.permission.RECEIVE_WAP_PUSH" />
<uses-permission android:name="android.permission.SEND_RESPOND_VIA_MESSAGE" />
```

### 2. 完善 MainActivity Intent Filter ⭐⭐⭐⭐⭐

添加了：
- `android.intent.action.SEND` action（之前只有 SENDTO）
- `android.intent.category.APP_MESSAGING` category（某些系统要求）

### 3. 添加 MMS 接收器 ⭐⭐⭐⭐

创建了 `MmsReceiver.kt` 用于接收彩信：
```xml
<receiver
    android:name=".util.MmsReceiver"
    android:exported="true"
    android:permission="android.permission.BROADCAST_WAP_PUSH">
    <intent-filter>
        <action android:name="android.provider.Telephony.WAP_PUSH_DELIVER_ACTION" />
        <data android:mimeType="application/vnd.wap.mms-message" />
    </intent-filter>
</receiver>
```

### 4. 添加快速回复服务 ⭐⭐⭐⭐

创建了 `SmsRespondService.kt` 用于处理快速回复：
```xml
<service
    android:name=".util.SmsRespondService"
    android:exported="true"
    android:permission="android.permission.SEND_RESPOND_VIA_MESSAGE">
    <intent-filter>
        <action android:name="android.intent.action.RESPOND_VIA_MESSAGE" />
        ...
    </intent-filter>
</service>
```

## 完整的配置清单

### ✅ 权限声明
- [x] `READ_SMS`
- [x] `RECEIVE_SMS`
- [x] `SEND_SMS`
- [x] `WRITE_SMS`
- [x] `RECEIVE_MMS` ✨新增
- [x] `RECEIVE_WAP_PUSH` ✨新增
- [x] `SEND_RESPOND_VIA_MESSAGE` ✨新增
- [x] `READ_CONTACTS`

### ✅ 组件声明

1. **MainActivity**
   - [x] `ACTION_VIEW`
   - [x] `ACTION_SEND` ✨新增
   - [x] `ACTION_SENDTO`
   - [x] `CATEGORY_DEFAULT`
   - [x] `CATEGORY_BROWSABLE`
   - [x] `CATEGORY_APP_MESSAGING` ✨新增
   - [x] 数据scheme: `sms`, `smsto`, `mms`, `mmsto`

2. **BroadcastReceiver**
   - [x] `SmsReceiver` - SMS接收（`SMS_RECEIVED`, `SMS_DELIVER_ACTION`）
   - [x] `MmsReceiver` - MMS接收（`WAP_PUSH_DELIVER_ACTION`）✨新增

3. **Service**
   - [x] `SmsSendService` - 短信发送（`ACTION_SENDTO`）
   - [x] `SmsRespondService` - 快速回复（`ACTION_RESPOND_VIA_MESSAGE`）✨新增

## 新增文件

1. ✨ `android/app/src/main/java/com/sms/tagger/util/MmsReceiver.kt`
   - 彩信接收器（Stub实现）

2. ✨ `android/app/src/main/java/com/sms/tagger/util/SmsRespondService.kt`
   - 快速回复服务（Stub实现）

## 修改的文件

1. ✅ `android/app/src/main/AndroidManifest.xml`
   - 添加了3个新权限
   - 添加了 `APP_MESSAGING` category
   - 添加了 `ACTION_SEND` action
   - 添加了 MMS 接收器声明
   - 添加了快速回复服务声明

2. ✅ `android/app/src/main/java/com/sms/tagger/util/SmsSendService.kt`
   - 添加了快速回复处理方法
   - 添加了 AppLogger 导入

## 测试步骤

### 1. 重新安装应用（重要！）

1. **卸载旧版本**
   - 完全卸载旧版本的应用

2. **安装新版本 APK**
   - 安装最新打包的 APK

3. **启动应用至少一次** ⚠️ 重要！
   - 必须至少启动应用一次
   - 系统在应用首次运行时才会扫描和注册组件

4. **授予所有权限**
   - 确保授予所有短信相关权限

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

# 检查系统是否识别应用为短信应用
adb shell cmd role list-apps-eligible-for-role SMS

# 检查应用的 Intent Filter
adb shell cmd package query-activities -a android.intent.action.SENDTO
adb shell cmd package query-activities -a android.intent.action.SENDTO | grep com.sms.tagger
```

#### 方法3：检查应用配置

```bash
# 查看应用的完整配置
adb shell dumpsys package com.sms.tagger
```

## 预期结果

应用应该出现在默认短信应用选择列表中，显示名称为"QuickCode"。

## 已知限制

1. **应用必须至少运行一次**
   - 系统在应用首次运行时才会扫描和注册组件
   - 安装后必须启动应用至少一次

2. **可能需要重启设备**
   - 某些设备需要重启才能识别新安装的短信应用

3. **某些厂商ROM可能有限制**
   - 华为/荣耀：可能需要在安全中心中额外授权
   - 小米：可能需要在应用管理中启用
   - OPPO/Vivo：可能有额外的安全检查

## 版本信息

- 修复版本：1.4.0
- 修复日期：2025-12-02
- APK生成时间：2025/12/2 16:38:11


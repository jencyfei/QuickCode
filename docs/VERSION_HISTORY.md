# 项目版本历史记录

## 📋 当前版本信息

### v1.4.0（当前版本）
- **Version Code**: 5
- **Version Name**: 1.4.0
- **构建日期**: 2025-12-02
- **状态**: ✅ 开发中

#### 主要变更
- ✅ 默认短信应用支持（实现成为系统默认短信应用的最小能力）
- ✅ 添加 SMS_DELIVER BroadcastReceiver 支持
- ✅ 创建 SmsSendService 和 SmsWriteHelper (Stub实现)
- ✅ 添加默认短信应用引导页面
- ✅ 修复短信接收后写入系统数据库（防止短信丢失）
- ✅ 添加 MMS 接收器和快速回复服务
- ✅ 完善 AndroidManifest.xml 配置（添加 RECEIVE_MMS, RECEIVE_WAP_PUSH 等权限）

#### 新增文件
1. `SmsSendService.kt` - 短信发送服务（Stub实现）
2. `SmsWriteHelper.kt` - 短信写入辅助工具
3. `SmsDefaultAppChecker.kt` - 默认短信应用检查工具
4. `DefaultSmsGuideScreen.kt` - 默认短信应用引导页面
5. `MmsReceiver.kt` - 彩信接收器
6. `SmsRespondService.kt` - 快速回复服务

#### 修改文件
1. `build.gradle` - 版本号更新为 1.4.0
2. `AndroidManifest.xml` - 添加权限和组件声明
3. `SmsReceiver.kt` - 支持 SMS_DELIVER，修复写入数据库逻辑
4. `SettingsScreen.kt` - 集成默认短信应用入口

#### 文档
- `docs/V1.4.0_RELEASE_NOTES.md` - 发布说明
- `docs/V1.4.0_BUILD_COMPLETE.md` - 构建完成总结
- `docs/V1.4.0_BACKUP_CHECKLIST.md` - 备份清单
- `docs/DEFAULT_SMS_APP_FAQ.md` - 常见问题解答
- `docs/DEFAULT_SMS_APP_IMPACT_ANALYSIS.md` - 影响分析

---

## 📜 历史版本记录

### v1.3.0
- **Version Code**: 未知
- **构建日期**: 2025-11-20 之前
- **状态**: ✅ 已完成

#### 主要变更
- ✅ 添加隐私政策弹窗
- ✅ 其他基础功能

#### Git 标签
- 标签: `v1.3.0`
- 提交哈希: `82c7cc6`

---

### v1.2.0
- **Version Code**: 未知
- **构建日期**: 2025-11-20 之前
- **状态**: ✅ 已完成

#### 主要变更
- ✅ 加解密功能添加
- ✅ 限制策略实施前备份

#### Git 标签
- 标签: `v1.2.0-encryption`
- 提交哈希: `e1782a5`

---

### v1.1.0
- **Version Code**: 未知
- **构建日期**: 2025-11-20 之前
- **状态**: ✅ 已完成

#### 主要变更
- ✅ 优化短信读取日志输出
- ✅ 每页输出前20条和最后20条详细信息

#### Git 标签
- 标签: `v1.1.0-release`, `v1.6-sms-paging-log-optimization`
- 提交哈希: `84f22bf`

#### 文档
- `COMPLETION_SUMMARY.txt` 中提到了 v1.1.0 相关信息

---

### v1.0.1
- **Version Code**: 未知
- **构建日期**: 2025-11-14
- **状态**: ✅ 已完成

#### 主要变更
- ✅ 快递日期分组功能
- ✅ 快递排序修复（2024年显示在2025年之后的问题）
- ✅ 标签过滤修复
- ✅ 日期提取优化
- ✅ 短信读取优化（分页查询机制）

#### APK 文件
- `app-release-20251114-v2.apk` (V1.0.1 - 修复版本，推荐使用)
- 大小: 未知

#### 文档
- `COMPLETION_SUMMARY.txt` - 项目完成总结
- 提到了一些HTML原型文件

---

### v1.0.0
- **Version Code**: 未知
- **构建日期**: 2025-11-14
- **状态**: ✅ 已完成（初始版本）

#### 主要变更
- ✅ 初始版本发布
- ✅ 基础功能实现

#### APK 文件
- `app-release-20251114.apk` (V1.0.0 - 初始版本)
- 大小: 未知

---

## 🔍 Git 提交历史（部分）

根据项目文档中的信息，以下是部分提交记录：

```
4ff4e13 (HEAD -> main, origin/main) 添加体验版(Trial)开发计划和相关文档
82c7cc6 (tag: v1.3.0) 添加隐私政策弹窗
e1782a5 (tag: v1.2.0-encryption) 加解密添加，限制策略前备份
649110b feat: 重构设置页面和激活弹窗UI，对齐HTML原型设计
84f22bf (tag: v1.6-sms-paging-log-optimization, tag: v1.1.0-release) 优化短信读取日志输出：每页输出前20条和最后20条详细信息
dc0d0e1 (tag: v1.5-in-app-logging) feat: add in-app logging tools and docs
bef120f docs: 添加完整的项目README文档
00ecb8c chore: 删除无用的脚本和构建产物文件
797d64c chore: 添加脚本文件清理分析报告
cc8f02f chore: 删除剩余的历史md文件
```

---

## 📝 代码注释中的版本信息

### SettingsScreen.kt
```kotlin
// 标题行：📨 QuickCode v1.2.0
```
**注意**: 这个注释显示的是 v1.2.0，但代码中实际使用的是 `BuildConfig.VERSION_NAME`，会自动显示当前版本。

### build.gradle
```gradle
versionCode 5
versionName "1.4.0"
```
**当前配置**: v1.4.0 (Version Code: 5)

---

## 📦 APK 文件历史

### v1.4.0
- Full版: `sms-agent-fullRelease-1.4.0.apk` (10.67 MB)
- Trial版: `sms-agent-trialRelease-1.4.0-trial.apk` (10.67 MB)
- 构建时间: 2025-12-02 16:38

### v1.0.1
- `app-release-20251114-v2.apk`
- 构建时间: 2025-11-14

### v1.0.0
- `app-release-20251114.apk`
- 构建时间: 2025-11-14

---

## 🔄 版本演进路线

```
v1.0.0 (初始版本)
    ↓
v1.0.1 (修复版本：日期分组、排序、标签过滤)
    ↓
v1.1.0 (短信读取优化)
    ↓
v1.2.0 (加解密功能)
    ↓
v1.3.0 (隐私政策弹窗)
    ↓
v1.4.0 (默认短信应用支持) ← 当前版本
```

---

## 📚 相关文档清单

### 版本发布文档
1. `docs/V1.4.0_RELEASE_NOTES.md` - v1.4.0 发布说明
2. `docs/V1.4.0_BUILD_COMPLETE.md` - v1.4.0 构建完成总结
3. `docs/V1.4.0_BACKUP_CHECKLIST.md` - v1.4.0 备份清单
4. `COMPLETION_SUMMARY.txt` - 项目完成总结（v1.0.1）

### 功能实现文档
1. `docs/DEFAULT_SMS_IMPLEMENTATION_TODO.md` - 默认短信应用TODO清单
2. `docs/DEFAULT_SMS_IMPLEMENTATION_SUMMARY.md` - 默认短信应用实施总结
3. `docs/DEFAULT_SMS_APP_FAQ.md` - 默认短信应用常见问题
4. `docs/DEFAULT_SMS_APP_IMPACT_ANALYSIS.md` - 影响分析

### 问题修复文档
1. `docs/DEFAULT_SMS_APP_LIST_FIX.md` - 应用列表显示问题修复
2. `docs/DEFAULT_SMS_APP_NOT_IN_LIST_FIX.md` - 应用不在列表中修复
3. `docs/HONOR_9X_CRASH_FIX.md` - 荣耀9X崩溃修复
4. `docs/10684_SMS_ISSUE_SUMMARY.md` - 10684短信问题总结

---

## ⚠️ 重要注释和说明

### 关于默认短信应用
- **目的**: 解决小米14等设备上无法读取所有短信的问题
- **实现**: 最小化实现，满足系统要求即可
- **影响**: 
  - ✅ 接收短信正常（已修复写入数据库）
  - ⚠️ 发送短信是Stub实现（不实际发送）

### 关于版本号
- 版本号在 `android/app/build.gradle` 中定义
- 设置页面显示从 `BuildConfig.VERSION_NAME` 读取
- APK文件名自动包含版本号

---

## 📅 版本时间线

| 版本 | 发布日期 | 主要功能 |
|------|---------|---------|
| v1.0.0 | 2025-11-14 | 初始版本 |
| v1.0.1 | 2025-11-14 | 日期分组、排序修复 |
| v1.1.0 | ~2025-11-20 | 短信读取优化 |
| v1.2.0 | ~2025-11-20 | 加解密功能 |
| v1.3.0 | ~2025-11-20 | 隐私政策弹窗 |
| v1.4.0 | 2025-12-02 | 默认短信应用支持（当前） |

---

**最后更新**: 2025-12-02  
**文档维护**: 建议每次版本发布时更新此文档


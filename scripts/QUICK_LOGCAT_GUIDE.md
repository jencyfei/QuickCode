# 快速查看 Logcat 日志指南

## 最简单的方法：使用 PowerShell 脚本

### 1. 基本使用

```powershell
# 查看 SMS 相关日志（实时）
.\scripts\view_sms_logcat.ps1

# 清除日志后查看
.\scripts\view_sms_logcat.ps1 -Clear

# 搜索特定关键词
.\scripts\view_sms_logcat.ps1 -Search "菜鸟驿站"

# 保存日志到文件
.\scripts\view_sms_logcat.ps1 -Save

# 搜索并保存
.\scripts\view_sms_logcat.ps1 -Search "1-4-4011" -Save -Clear
```

### 2. 使用 ADB 命令（如果没有脚本）

```powershell
# 检查设备连接
adb devices

# 清除日志
adb logcat -c

# 查看 SMS 相关日志
adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S

# 搜索特定关键词
adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S | Select-String "菜鸟驿站"
```

### 3. 使用 Android Studio（推荐给开发者）

1. **打开 Android Studio**
2. **连接设备**
3. **打开 Logcat**：点击底部 "Logcat" 标签
4. **选择应用**：下拉菜单选择 `com.sms.tagger`
5. **过滤日志**：搜索框输入 `SmsReader` 或 `菜鸟驿站`

## 调试当前问题的步骤

### 步骤 1：准备

```powershell
# 运行脚本（清除旧日志并开始监控）
.\scripts\view_sms_logcat.ps1 -Clear -Search "菜鸟驿站|1-4-4011"
```

### 步骤 2：操作应用

1. 打开应用
2. 进入"短信列表"页面
3. 等待加载完成

### 步骤 3：查看结果

在日志中查找：
- `🔍 找到目标短信！` ✅ 找到了
- `⚠️ 未找到目标短信` ❌ 没找到
- `包含'菜鸟驿站'的短信共 X 条` 📊 统计信息

## 常用命令速查

```powershell
# 最常用：查看并搜索
adb logcat -c && adb logcat SmsReader:D SmsListScreen:D *:S | Select-String "菜鸟驿站"

# 保存日志
adb logcat SmsReader:D SmsListScreen:D *:S > sms_log.txt

# 查看保存的日志
cat sms_log.txt | Select-String "目标短信"
```

## 如果看不到日志？

1. **检查设备连接**：`adb devices`
2. **检查 USB 调试**：手机设置 → 开发者选项 → USB 调试
3. **授权电脑**：手机上弹出的 USB 调试授权提示，点击"允许"

---

**提示**：使用脚本最方便！直接运行 `.\scripts\view_sms_logcat.ps1 -Clear -Search "菜鸟驿站"` 即可。


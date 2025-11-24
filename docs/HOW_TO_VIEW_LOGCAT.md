# 如何查看 Android Logcat 日志

## 方法一：使用 Android Studio（推荐）

### 1. 打开 Logcat 窗口

1. **启动 Android Studio**
2. **连接设备**：使用 USB 线连接手机，或启动模拟器
3. **打开 Logcat**：
   - 方法1：点击底部工具栏的 **"Logcat"** 标签
   - 方法2：菜单栏 → **View** → **Tool Windows** → **Logcat**
   - 方法3：快捷键：`Alt + 6`（Windows）或 `Cmd + 6`（Mac）

### 2. 配置 Logcat 过滤器

#### 快速过滤：
1. **选择应用包名**：在下拉菜单选择 `com.sms.tagger`
2. **过滤标签**：在搜索框输入过滤关键词
   - 搜索 `SmsReader` 查看短信读取日志
   - 搜索 `SmsListScreen` 查看短信列表日志
   - 搜索 `ExpressExtractor` 查看快递提取日志
   - 搜索 `菜鸟驿站` 查看菜鸟驿站相关日志
   - 搜索 `1-4-4011` 查看目标短信日志

#### 创建自定义过滤器：

1. **点击过滤器下拉菜单** → **Edit Filter Configuration**
2. **点击 + 号** 创建新过滤器
3. **配置过滤器**：
   - **Name**：`SMS Debug`（自定义名称）
   - **Log Tag**：`SmsReader|SmsListScreen|ExpressExtractor`
   - **Package Name**：`com.sms.tagger`
   - **Log Level**：`Debug` 或 `Verbose`（查看更多日志）
4. **点击 OK** 保存
5. **选择新创建的过滤器**

### 3. 查看日志级别

Logcat 显示不同级别的日志：
- **Verbose (V)**：详细日志（白色/灰色）
- **Debug (D)**：调试日志（蓝色）
- **Info (I)**：信息日志（绿色）
- **Warn (W)**：警告日志（橙色）
- **Error (E)**：错误日志（红色）

### 4. 搜索和过滤日志

在 Logcat 搜索框中可以输入：
- **标签名**：`SmsReader`、`SmsListScreen`
- **关键词**：`菜鸟驿站`、`1-4-4011`、`目标短信`
- **日志级别**：`level:ERROR`、`level:WARN`
- **包名**：`package:com.sms.tagger`

## 方法二：使用命令行 ADB（适合调试）

### 1. 安装 ADB

如果已安装 Android Studio，ADB 通常已包含在：
- Windows: `C:\Users\你的用户名\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- 或在 Android Studio 的 SDK 目录中

### 2. 连接设备

```powershell
# 检查设备连接
adb devices

# 应该看到类似输出：
# List of devices attached
# ABC123XYZ    device
```

如果设备未连接：
1. 在手机上启用 **USB 调试**
2. 允许电脑的 USB 调试授权
3. 重新运行 `adb devices`

### 3. 查看所有日志

```powershell
# 实时查看所有日志
adb logcat

# 清空日志并开始查看
adb logcat -c && adb logcat
```

### 4. 过滤特定标签

```powershell
# 只查看 SMS 相关日志
adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S

# 解释：
# SmsReader:D    - 显示 SmsReader 标签的 Debug 及以上级别日志
# SmsListScreen:D - 显示 SmsListScreen 标签的 Debug 及以上级别日志
# ExpressExtractor:D - 显示 ExpressExtractor 标签的 Debug 及以上级别日志
# *:S           - 其他标签静默（Suppress）
```

### 5. 搜索特定关键词

```powershell
# 搜索包含"菜鸟驿站"的日志
adb logcat | Select-String "菜鸟驿站"

# 搜索包含"1-4-4011"的日志
adb logcat | Select-String "1-4-4011"

# 搜索目标短信标记
adb logcat | Select-String "🔍|目标短信"
```

### 6. 保存日志到文件

```powershell
# 保存所有日志到文件
adb logcat > logcat_all.txt

# 保存过滤后的日志
adb logcat SmsReader:D SmsListScreen:D *:S > logcat_sms.txt

# 保存并实时显示
adb logcat SmsReader:D SmsListScreen:D *:S | Tee-Object -FilePath logcat_sms.txt
```

### 7. 清除日志缓冲区

```powershell
# 清除所有日志
adb logcat -c

# 然后重新查看
adb logcat
```

## 方法三：使用 PowerShell 脚本（方便快捷）

### 创建查看日志的脚本

创建一个 PowerShell 脚本 `view_logcat.ps1`：

```powershell
# view_logcat.ps1 - 查看 SMS 相关日志

# 配置 ADB 路径（如果需要）
$adbPath = "adb"  # 如果 adb 在 PATH 中，直接用 "adb"
# $adbPath = "C:\Users\你的用户名\AppData\Local\Android\Sdk\platform-tools\adb.exe"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  查看 SMS 相关日志" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# 检查设备连接
Write-Host "检查设备连接..." -ForegroundColor Yellow
$devices = & $adbPath devices
Write-Host $devices

if ($devices -notmatch "device$") {
    Write-Host "错误: 没有检测到设备！请连接设备并启用 USB 调试。" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "开始查看日志（按 Ctrl+C 停止）..." -ForegroundColor Green
Write-Host ""

# 清空日志
& $adbPath logcat -c

# 查看过滤后的日志
& $adbPath logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S
```

使用方法：
```powershell
# 运行脚本
.\view_logcat.ps1

# 或者直接查看特定关键词
.\view_logcat.ps1 | Select-String "菜鸟驿站"
```

## 方法四：在应用中查看日志（开发中）

可以在应用中添加一个"查看日志"功能，显示最近的日志记录。

## 常用日志过滤命令

### 查看短信读取相关日志

```powershell
# 完整命令：查看 SMS 相关日志
adb logcat -c && adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S

# 搜索目标短信
adb logcat | Select-String "1-4-4011|菜鸟驿站|目标短信|🔍"
```

### 查看特定应用的日志

```powershell
# 只查看我们应用的日志
adb logcat --pid=$(adb shell pidof -s com.sms.tagger)

# 或者
adb logcat | Select-String "com.sms.tagger"
```

### 查看错误和警告

```powershell
# 只查看 Error 和 Warning
adb logcat *:E *:W

# 或过滤特定标签的错误
adb logcat SmsReader:E SmsListScreen:E ExpressExtractor:E *:S
```

### 实时搜索并高亮

```powershell
# PowerShell 中高亮显示关键词
adb logcat | ForEach-Object {
    if ($_ -match "菜鸟驿站|1-4-4011|目标短信") {
        Write-Host $_ -ForegroundColor Yellow
    } else {
        Write-Host $_
    }
}
```

## 调试当前问题的步骤

### 1. 准备环境

```powershell
# 连接设备
adb devices

# 清除旧日志
adb logcat -c
```

### 2. 开始监控日志

```powershell
# 在新窗口中运行（保持运行）
adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S > logcat_debug.txt
```

### 3. 操作应用

1. 打开应用
2. 进入"短信列表"页面
3. 等待加载完成
4. 尝试搜索"1-4-4011"或"菜鸟驿站"

### 4. 查看结果

```powershell
# 在另一个窗口中查看保存的日志
cat logcat_debug.txt | Select-String "菜鸟驿站|1-4-4011|目标短信|🔍"

# 或者打开文件查看
notepad logcat_debug.txt
```

### 5. 关键日志标记

在日志中查找以下标记：
- `🔍 找到目标短信！` - 成功读取到目标短信
- `⚠️ 在读取的 X 条短信中未找到目标短信` - 未读取到
- `包含'菜鸟驿站'的短信共 X 条` - 菜鸟驿站短信统计
- `========== 开始读取短信 ==========` - 读取开始
- `✅ 成功读取 X 条短信` - 读取完成

## 常见问题

### Q1: 看不到任何日志？

**A**: 检查以下几点：
1. 设备是否正确连接：`adb devices`
2. USB 调试是否已启用
3. 是否有读取日志的权限
4. 应用是否正在运行

### Q2: 日志太多，找不到需要的？

**A**: 使用过滤器：
```powershell
# 只查看特定标签
adb logcat SmsReader:D *:S

# 搜索关键词
adb logcat | Select-String "关键词"
```

### Q3: 日志刷新太快？

**A**: 保存到文件后查看：
```powershell
adb logcat > logcat.txt
# 停止后查看文件
```

### Q4: 想查看历史日志？

**A**: Android 系统只保留最近的日志。如果需要历史日志，需要提前开始记录。

## 快速参考

### 最常用的命令

```powershell
# 查看 SMS 相关日志（推荐）
adb logcat -c && adb logcat SmsReader:D SmsListScreen:D ExpressExtractor:D *:S

# 搜索目标短信
adb logcat | Select-String "1-4-4011|菜鸟驿站"

# 保存日志到文件
adb logcat SmsReader:D SmsListScreen:D *:S > sms_log.txt
```

---

**提示**：如果使用 Android Studio 开发，最方便的方法是直接使用 Logcat 窗口，并设置过滤器。


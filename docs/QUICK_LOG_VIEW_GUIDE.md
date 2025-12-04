# 快速查看Logcat日志指南

## 重要提示

**"USB调试应用"功能不是查看Logcat的必需步骤！**

您可以直接使用以下方法查看日志，无需在"USB调试应用"中选择应用。

---

## 方法1：使用ADB命令（推荐）

### 步骤1：连接设备并验证

```powershell
# 检查设备是否连接
adb devices
```

**预期输出：**
```
List of devices attached
XXXXXXXX    device
```

如果显示 `no devices` 或 `unauthorized`，请参考设备连接检查清单。

---

### 步骤2：清除旧日志并开始监听

```powershell
# 清除旧日志
adb logcat -c

# 开始监听，过滤项目相关日志
adb logcat | Select-String -Pattern "SmsReader|ExpressExtractor|10684|9-5-5038|菜鸟驿站"
```

---

### 步骤3：操作应用，观察日志

1. 在手机上打开应用
2. 进入"快递取件码"页面
3. 观察命令行输出的日志

---

## 方法2：保存日志到文件

```powershell
# 清除旧日志
adb logcat -c

# 然后在手机上操作应用...

# 保存日志到文件（按 Ctrl+C 停止监听）
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
adb logcat > "logcat_$timestamp.txt"
```

---

## 方法3：只查看最近的日志

```powershell
# 查看最近1000行日志
adb logcat -d -t 1000 | Select-String -Pattern "10684|9-5-5038|菜鸟驿站"
```

**参数说明：**
- `-d` : 输出日志后退出（不持续监听）
- `-t 1000` : 只显示最近1000行

---

## 方法4：使用Android Studio（最简单）

1. **打开Android Studio**
2. **连接手机**（确保USB调试已授权）
3. **点击底部工具栏的"Logcat"标签**
4. **选择设备和应用**
   - 设备选择器：选择您的手机
   - 包名选择器：选择 `com.sms.tagger` 或选择"Show only selected application"
5. **查看日志**
   - 可以直接搜索关键词
   - 可以按日志级别过滤
   - 可以保存日志文件

**优势：**
- ✅ 图形界面，易于操作
- ✅ 自动过滤应用日志
- ✅ 支持搜索和过滤
- ✅ 可以保存日志文件

---

## 针对当前问题的命令

### 查找10684发件人的短信

```powershell
adb logcat -c
adb logcat | Select-String -Pattern "10684"
```

### 查找取件码9-5-5038

```powershell
adb logcat -c
adb logcat | Select-String -Pattern "9-5-5038"
```

### 查找菜鸟驿站相关日志

```powershell
adb logcat -c
adb logcat | Select-String -Pattern "菜鸟驿站|cainiao"
```

### 综合查找（包含所有关键词）

```powershell
adb logcat -c
adb logcat | Select-String -Pattern "SmsReader|ExpressExtractor|10684|9-5-5038|菜鸟驿站|cainiao"
```

---

## 故障排查

### 问题1：`adb devices` 显示 `no devices`

**解决方法：**
1. 检查USB线是否连接
2. 尝试不同的USB端口
3. 在手机上启用USB调试
4. 重启ADB服务：
   ```powershell
   adb kill-server
   adb start-server
   adb devices
   ```

### 问题2：显示 `unauthorized`

**解决方法：**
1. 在手机上撤销USB调试授权
2. 重新连接USB
3. 手机上弹出授权对话框时，勾选"始终允许"
4. 点击"确定"

### 问题3：日志太多，无法找到目标

**解决方法：**
1. 使用更精确的过滤条件
2. 先清除日志，然后操作应用：
   ```powershell
   adb logcat -c
   # 然后在手机上操作应用
   adb logcat | Select-String -Pattern "关键词"
   ```

---

## 快速参考

### 常用的过滤关键词

| 关键词 | 说明 |
|--------|------|
| `SmsReader` | 短信读取相关 |
| `ExpressExtractor` | 快递信息提取相关 |
| `10684` | 发件人号码 |
| `9-5-5038` | 取件码 |
| `菜鸟驿站` | 快递公司 |
| `cainiao` | 快递公司（英文） |

### 常用的ADB命令

```powershell
# 检查设备连接
adb devices

# 清除日志
adb logcat -c

# 查看所有日志
adb logcat

# 查看最近1000行日志
adb logcat -d -t 1000

# 只查看错误和警告
adb logcat *:E *:W

# 保存日志到文件
adb logcat > logcat.txt
```

---

## 总结

**关键点：**
- ✅ **无需选择"USB调试应用"**
- ✅ **直接使用 `adb logcat` 命令**
- ✅ **Android Studio的Logcat窗口最简单**
- ✅ **确保设备已连接并授权USB调试**

**推荐工作流程：**
1. 连接设备并验证 (`adb devices`)
2. 清除旧日志 (`adb logcat -c`)
3. 使用过滤条件查看日志
4. 操作应用，观察日志输出


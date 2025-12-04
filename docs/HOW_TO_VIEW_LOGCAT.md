# 如何查看 Logcat 日志

## 方法一：使用 ADB 命令（推荐用于命令行调试）

### 1. 基本命令

#### 查看所有日志
```bash
adb logcat
```

#### 查看并实时滚动（类似 `tail -f`）
```bash
adb logcat -c && adb logcat
```
- `-c` 参数：清除之前的日志，从干净状态开始

#### 查看并保存到文件
```bash
adb logcat > logcat_output.txt
```

---

### 2. 过滤特定标签的日志

#### 过滤本项目相关的日志
```bash
adb logcat | grep -i "SmsReader\|ExpressExtractor\|ExpressScreen\|SmsListScreen"
```

#### Windows PowerShell 版本
```powershell
adb logcat | Select-String -Pattern "SmsReader|ExpressExtractor|ExpressScreen|SmsListScreen"
```

#### 使用标签过滤（更高效）
```bash
adb logcat SmsReader:D ExpressExtractor:D ExpressScreen:D SmsListScreen:D *:S
```
- `SmsReader:D` 表示显示 SmsReader 标签的 Debug 级别及以上
- `*:S` 表示静默其他所有标签（减少噪音）

---

### 3. 按日志级别过滤

#### 查看 Error 和 Warning
```bash
adb logcat *:E *:W
```

#### 查看 Debug、Info、Warning、Error
```bash
adb logcat *:D
```

#### 日志级别说明
- `V` = Verbose（最详细）
- `D` = Debug（调试）
- `I` = Info（信息）
- `W` = Warning（警告）
- `E` = Error（错误）
- `S` = Silent（静默，不显示）

---

### 4. 查看特定应用的日志

#### 按包名过滤（本项目）
```bash
adb logcat | grep "com.sms.tagger"
```

#### 或者使用进程ID
```bash
# 1. 先找到应用的进程ID
adb shell pidof com.sms.tagger

# 2. 使用进程ID过滤
adb logcat --pid=<进程ID>
```

---

### 5. 查找特定内容的日志

#### 查找包含 "10684" 的日志
```bash
adb logcat | grep "10684"
```

#### 查找包含 "9-5-5038" 的日志
```bash
adb logcat | grep "9-5-5038"
```

#### 查找包含 "菜鸟驿站" 的日志
```bash
adb logcat | grep "菜鸟驿站"
```

#### Windows PowerShell 版本
```powershell
adb logcat | Select-String -Pattern "10684"
adb logcat | Select-String -Pattern "9-5-5038"
adb logcat | Select-String -Pattern "菜鸟驿站"
```

---

### 6. 组合过滤（查找短信读取相关的日志）

#### 清除日志 + 查找短信读取相关日志
```bash
adb logcat -c && adb logcat | grep -i "sms\|express\|菜鸟\|10684\|9-5-5038"
```

#### Windows PowerShell 版本
```powershell
adb logcat -c; adb logcat | Select-String -Pattern "sms|express|菜鸟|10684|9-5-5038"
```

---

## 方法二：使用 Android Studio（推荐用于开发调试）

### 1. 打开 Logcat 窗口

1. **启动 Android Studio**
2. **连接设备**（或启动模拟器）
3. **打开 Logcat 窗口**：
   - 方式1：点击底部工具栏的 `Logcat` 标签
   - 方式2：菜单 `View` → `Tool Windows` → `Logcat`
   - 方式3：快捷键 `Alt + 6` (Windows) 或 `Cmd + 6` (Mac)

---

### 2. 过滤日志

#### 按标签过滤
1. 在 Logcat 窗口顶部，找到过滤器下拉菜单
2. 选择 `Edit Filter Configuration`
3. 创建一个新过滤器：
   - **Filter Name**: `SMS App`
   - **Log Tag**: `SmsReader|ExpressExtractor|ExpressScreen|SmsListScreen`
   - **Log Level**: `Debug` 或 `Verbose`

#### 按包名过滤
1. 在 Logcat 窗口顶部，找到设备选择器
2. 选择你的应用包名：`com.sms.tagger`

#### 按搜索关键词过滤
1. 在 Logcat 窗口顶部的搜索框中
2. 输入关键词：`10684`、`9-5-5038`、`菜鸟驿站` 等
3. 支持正则表达式搜索

---

### 3. 保存日志

#### 在 Android Studio 中保存
1. 右键点击 Logcat 窗口
2. 选择 `Export Logcat to File...`
3. 选择保存位置和文件名

---

## 方法三：使用 Android 设备的日志查看工具

### 使用 ADB 直接查看设备上的日志文件

```bash
# 查看系统日志文件
adb shell logcat -d > logcat_full.txt

# -d 参数：输出日志后退出（不持续监听）
```

---

## 针对本项目问题的具体命令

### 场景1：查找为什么短信未被识别

#### 清除日志并开始监听
```bash
adb logcat -c
```

#### 然后运行以下命令之一：

**查找短信读取相关的所有日志**
```bash
adb logcat | grep -i "SmsReader\|短信\|读取\|readAllSms"
```

**查找10684发件人的短信**
```bash
adb logcat | grep -i "10684"
```

**查找取件码提取相关的日志**
```bash
adb logcat | grep -i "ExpressExtractor\|取件码\|pickup\|9-5-5038"
```

**查找菜鸟驿站相关的日志**
```bash
adb logcat | grep -i "菜鸟\|cainiao"
```

**综合查找（包含所有关键词）**
```bash
adb logcat | grep -i "SmsReader\|ExpressExtractor\|10684\|9-5-5038\|菜鸟\|cainiao\|短信列表"
```

---

### 场景2：保存完整的调试日志

#### 清除旧日志，启动应用，然后保存日志
```bash
# 1. 清除旧日志
adb logcat -c

# 2. 打开应用（手动在设备上打开）

# 3. 等待一段时间让应用加载短信

# 4. 保存日志到文件
adb logcat -d > logcat_sms_debug_$(date +%Y%m%d_%H%M%S).txt
```

**Windows PowerShell 版本**
```powershell
# 1. 清除旧日志
adb logcat -c

# 2. 打开应用（手动在设备上打开）

# 3. 等待一段时间

# 4. 保存日志
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
adb logcat -d > "logcat_sms_debug_$timestamp.txt"
```

---

### 场景3：实时监控应用启动和短信读取过程

```bash
# 清除日志
adb logcat -c

# 开始监听，过滤项目相关日志
adb logcat | grep -i "SmsReader\|ExpressExtractor\|ExpressScreen\|菜鸟\|10684\|9-5-5038"

# 然后在设备上：
# 1. 打开应用
# 2. 进入"快递取件码"页面
# 3. 观察日志输出
```

---

## 有用的 ADB 命令组合

### 1. 清除日志 + 实时查看 + 保存到文件
```bash
adb logcat -c && adb logcat | tee logcat_output.txt | grep -i "SmsReader\|ExpressExtractor"
```

### 2. 只查看 Error 和 Warning
```bash
adb logcat *:E *:W | grep -i "sms\|express"
```

### 3. 查看最近50行日志
```bash
adb logcat -d -t 50 | grep -i "sms\|express"
```

### 4. 按时间过滤（查看特定时间段的日志）
```bash
# 查看最近10分钟的日志
adb logcat -t "$(date +%H:%M:%S.000)"
```

---

## Windows PowerShell 专用命令

### 1. 实时查看并过滤
```powershell
adb logcat | Select-String -Pattern "SmsReader|ExpressExtractor|10684|9-5-5038" | Tee-Object -FilePath "logcat_filtered.txt"
```

### 2. 清除日志并开始监听
```powershell
adb logcat -c; adb logcat | Select-String -Pattern "SmsReader|ExpressExtractor"
```

### 3. 保存所有日志
```powershell
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
adb logcat -d > "logcat_full_$timestamp.txt"
```

---

## 常见问题

### Q1: 日志太多，如何减少噪音？

**A**: 使用标签过滤，只显示需要的日志
```bash
adb logcat SmsReader:D ExpressExtractor:D ExpressScreen:D *:S
```

### Q2: 如何查看历史日志？

**A**: 使用 `-d` 参数，输出后退出
```bash
adb logcat -d > logcat_history.txt
```

### Q3: 日志显示乱码怎么办？

**A**: 确保终端支持UTF-8编码，或者使用 Android Studio 的 Logcat

### Q4: 如何查找特定时间段的日志？

**A**: 可以使用 `-t` 参数指定行数，或者使用 `-T` 指定时间
```bash
# 查看最近1000行
adb logcat -d -t 1000
```

---

## 快速参考

### 本项目的关键日志标签

| 标签 | 说明 |
|------|------|
| `SmsReader` | 短信读取相关日志 |
| `ExpressExtractor` | 快递信息提取相关日志 |
| `ExpressScreen` | 快递页面相关日志 |
| `SmsListScreen` | 短信列表页面相关日志 |
| `ExpressCompanyDetector` | 快递公司识别相关日志 |

### 关键搜索关键词

- `10684` - 发件人号码
- `9-5-5038` - 取件码
- `菜鸟驿站` - 快递公司
- `读取短信` - 短信读取过程
- `提取快递信息` - 快递信息提取
- `取件码` - 取件码提取

---

**提示**：建议在诊断问题时，先清除日志（`adb logcat -c`），然后重新打开应用，这样可以更清晰地看到问题的发生过程。


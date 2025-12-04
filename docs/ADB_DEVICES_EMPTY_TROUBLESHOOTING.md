# ADB设备连接问题排查指南

## 问题描述

`adb devices` 返回空列表，但应用已安装在手机上。

```
List of devices attached
（空）
```

---

## 完整排查步骤

### 步骤1：检查USB连接

1. **确认USB线已连接**
   - 手机通过USB线连接到电脑
   - 尝试更换USB端口（建议使用USB 2.0端口，兼容性更好）
   - 尝试更换USB线（某些线只支持充电，不支持数据传输）

2. **检查USB连接模式**
   - 手机上可能弹出"USB连接方式"通知
   - 选择"文件传输"或"MTP模式"
   - 不要选择"仅充电"

---

### 步骤2：启用USB调试

1. **进入开发者选项**
   - 打开手机"设置"
   - 进入"关于手机"或"系统信息"
   - 找到"版本号"或"内部版本号"
   - **连续点击7次**（会提示"您已处于开发者模式"）

2. **启用USB调试**
   - 返回"设置"主界面
   - 找到"开发者选项"（通常在"系统"或"其他设置"中）
   - 启用"USB调试"
   - 启用"USB安装"（如果需要）
   - 启用"通过USB验证应用"（如果看到）

3. **重要设置**
   - 某些手机需要启用"仅充电模式下允许ADB调试"（小米手机常见）
   - 某些手机需要启用"USB调试（安全设置）"（华为手机常见）

---

### 步骤3：授权电脑

1. **首次连接授权**
   - 连接USB后，手机上会弹出"允许USB调试吗？"对话框
   - **勾选"始终允许这台计算机"**
   - 点击"确定"

2. **如果已经授权但连接失败**
   - 进入"开发者选项"
   - 找到"撤销USB调试授权"或"撤销所有USB调试授权"
   - 点击撤销
   - 重新连接USB
   - 重新授权

---

### 步骤4：重启ADB服务

```powershell
# 停止ADB服务
adb kill-server

# 等待几秒
Start-Sleep -Seconds 2

# 启动ADB服务
adb start-server

# 检查设备
adb devices
```

---

### 步骤5：检查设备管理器（Windows）

1. **打开设备管理器**
   - 右键"此电脑" → "管理" → "设备管理器"
   - 或按 `Win + X` → 选择"设备管理器"

2. **检查Android设备**
   - 连接手机后，查看是否有"Android Phone"或"便携设备"
   - 如果有黄色感叹号，说明驱动有问题

3. **安装/更新驱动**
   - 某些手机品牌需要安装专用驱动：
     - **小米**：需要安装"小米手机助手"或"Mi USB Driver"
     - **华为**：需要安装"华为手机助手"或"HiSuite"
     - **OPPO/VIVO**：可能需要安装对应品牌的手机助手
     - **三星**：需要安装"Samsung USB Driver"
     - **通用**：可以尝试安装"通用Android驱动"

---

### 步骤6：尝试不同的连接方式

#### 方式A：使用不同的USB端口

```powershell
# 拔掉USB，换一个端口，重新连接
# 然后运行
adb devices
```

#### 方式B：使用WiFi连接（如果USB有问题）

```powershell
# 1. 先用USB连接一次（确保能连接）
# 2. 启用WiFi调试
adb tcpip 5555

# 3. 获取手机IP地址
#    在手机上：设置 → WLAN → 已连接的WiFi → 查看IP地址
#    假设IP是 192.168.1.100

# 4. 连接WiFi
adb connect 192.168.1.100:5555

# 5. 断开USB，验证连接
adb devices

# 应该看到类似：
# List of devices attached
# 192.168.1.100:5555    device
```

---

### 步骤7：检查防火墙和杀毒软件

1. **Windows防火墙**
   - 可能需要允许ADB通过防火墙
   - 进入"Windows安全中心" → "防火墙和网络保护" → "允许应用通过防火墙"
   - 查找并启用"Android Debug Bridge"

2. **杀毒软件**
   - 某些杀毒软件可能阻止ADB
   - 尝试暂时禁用杀毒软件测试
   - 或添加ADB到白名单

---

### 步骤8：检查ADB版本

```powershell
# 检查ADB版本
adb version

# 如果版本太老，可能需要更新
# ADB通常随Android SDK一起更新
```

---

### 步骤9：使用USB调试工具验证

如果以上步骤都不行，可以尝试：

1. **使用手机厂商的助手软件**
   - 小米：小米手机助手
   - 华为：华为手机助手
   - OPPO：OPPO手机助手
   - VIVO：VIVO手机助手
   - 通用：91助手、360手机助手等

2. **如果助手软件能连接，说明硬件没问题**
   - 可能是ADB配置问题
   - 可能是驱动问题

---

## 手机品牌特定问题

### 小米手机

1. **启用"仅充电模式下允许ADB调试"**
   - 开发者选项 → 启用此选项

2. **安装小米USB驱动**
   - 下载"小米手机助手"安装

3. **关闭MIUI优化**（如果启用）
   - 开发者选项 → 关闭"MIUI优化"

### 华为/荣耀手机

1. **启用"USB调试（安全设置）"**
   - 开发者选项 → 启用此选项

2. **安装华为手机助手**
   - 下载"华为手机助手"或"HiSuite"安装

3. **关闭"纯净模式"**（如果有）
   - 设置 → 系统和更新 → 关闭纯净模式

### OPPO/OnePlus手机

1. **启用"USB调试（安全设置）"**
   - 开发者选项 → 启用此选项

2. **安装OPPO手机助手**（如果需要）

### VIVO手机

1. **启用"USB调试（安全设置）"**
   - 开发者选项 → 启用此选项

2. **关闭"纯净模式"**（如果有）

### 三星手机

1. **安装Samsung USB Driver**
   - 从Samsung官网下载

2. **使用Samsung Smart Switch验证连接**

---

## 快速诊断命令

运行以下命令进行快速诊断：

```powershell
# 1. 检查ADB服务状态
adb version
adb kill-server
adb start-server

# 2. 检查设备连接
adb devices

# 3. 检查设备详细信息（如果连接成功）
adb devices -l

# 4. 尝试查看设备状态
adb get-state

# 5. 如果连接成功，查看设备信息
adb shell getprop ro.product.model
```

---

## 替代方案：使用WiFi连接

如果USB连接一直有问题，可以使用WiFi连接：

### 前提条件
- USB连接曾经成功过一次（用于初始设置）

### 步骤

```powershell
# 1. 先用USB连接（如果可能）
adb devices

# 2. 启用WiFi调试
adb tcpip 5555

# 3. 断开USB

# 4. 在手机上查看WiFi IP地址
#    设置 → WLAN → 点击已连接的WiFi → 查看IP地址
#    假设IP是 192.168.1.100

# 5. 通过WiFi连接
adb connect 192.168.1.100:5555

# 6. 验证连接
adb devices
```

---

## 最终检查清单

- [ ] USB线已连接且支持数据传输
- [ ] 手机上已启用"开发者选项"
- [ ] 已启用"USB调试"
- [ ] 已授权电脑（弹出对话框时勾选"始终允许"）
- [ ] Windows设备管理器中能看到Android设备（无感叹号）
- [ ] 已重启ADB服务（`adb kill-server && adb start-server`）
- [ ] 尝试了不同的USB端口
- [ ] 防火墙/杀毒软件未阻止ADB
- [ ] 手机品牌特定设置已正确配置

---

## 如果仍然无法连接

1. **尝试其他电脑**
   - 如果在其他电脑上能连接，说明是当前电脑的问题
   - 如果在其他电脑上也不能连接，说明是手机或USB线的问题

2. **重新安装ADB**
   - 从Android SDK Platform Tools下载最新版本
   - 或通过Android Studio更新SDK

3. **联系手机厂商技术支持**
   - 某些品牌的手机可能需要特殊配置

---

## 关于APK文件路径

**重要**：APK文件的正确路径是：

- **Full版**: `android/app/build/outputs/apk/full/release/sms-agent-fullRelease-1.3.0.apk`
- **Trial版**: `android/app/build/outputs/apk/trial/release/sms-agent-trialRelease-1.3.0-trial.apk`
- **Debug版**: `android/app/build/outputs/apk/debug/sms-agent-debug-1.3.0.apk`

**文件名格式说明**：
- Gradle构建工具会根据`build.gradle`中的配置自动生成文件名
- 文件名包含：应用名称、构建变体（full/trial/debug）、版本号
- 不同的构建配置会产生不同的文件名

---

## 下一步

完成设备连接后，您可以：

1. **查看Logcat日志**
   ```powershell
   adb logcat | Select-String -Pattern "SmsReader|10684|9-5-5038"
   ```

2. **重新安装应用**（如果需要）
   ```powershell
   adb install -r android/app/build/outputs/apk/full/release/sms-agent-fullRelease-1.3.0.apk
   ```

3. **查看设备上的应用信息**
   ```powershell
   adb shell pm list packages | Select-String "sms.tagger"
   adb shell dumpsys package com.sms.tagger
   ```

---

需要我帮您检查具体是哪个步骤出了问题吗？


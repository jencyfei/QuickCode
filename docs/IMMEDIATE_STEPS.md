# 立即执行的步骤 - ADB设备连接

## 当前问题

- ✅ ADB服务正常运行
- ❌ 未检测到Android设备
- ⚠️ Windows设备管理器中可能有未知USB设备

---

## 立即执行的步骤（按顺序）

### 步骤1：检查手机上的USB连接模式

**操作：**
1. 确保手机已通过USB线连接到电脑
2. 在手机上**下拉通知栏**
3. 找到"USB连接"或"USB用于"的通知
4. 点击它，选择：
   - ✅ **"文件传输"** 或 **"MTP模式"**
   - ❌ 不要选择"仅充电"

**如果看不到通知：**
- 进入：设置 → 连接与共享 → USB连接（或类似路径）
- 手动选择"文件传输"模式

**完成此步骤后，等待5秒，然后运行：**
```powershell
adb devices
```

---

### 步骤2：启用USB调试并授权

**操作：**

#### 2.1 启用开发者选项（如果未启用）
1. 设置 → 关于手机
2. 连续点击"版本号"7次
3. 提示"您已处于开发者模式"

#### 2.2 启用USB调试
1. 返回设置 → 开发者选项
2. 启用"USB调试"
3. 如果是小米手机，同时启用"仅充电模式下允许ADB调试"

#### 2.3 授权电脑
1. **拔掉USB线，重新连接**
2. 手机上应该弹出"允许USB调试吗？"对话框
3. **勾选"始终允许这台计算机"**
4. 点击"确定"

**完成此步骤后，运行：**
```powershell
adb kill-server
adb start-server
adb devices
```

---

### 步骤3：检查品牌特定设置

根据您的手机品牌，完成以下设置：

#### 小米手机
- [ ] 开发者选项 → 启用"仅充电模式下允许ADB调试"
- [ ] 开发者选项 → 关闭"MIUI优化"（可选）

#### 华为/荣耀手机
- [ ] 开发者选项 → 启用"USB调试（安全设置）"
- [ ] 设置 → 系统和更新 → 关闭"纯净模式"

#### OPPO/OnePlus手机
- [ ] 开发者选项 → 启用"USB调试（安全设置）"

#### VIVO手机
- [ ] 开发者选项 → 启用"USB调试（安全设置）"

**完成此步骤后，重新连接USB，然后运行：**
```powershell
adb devices
```

---

### 步骤4：检查USB线和端口

**尝试以下操作：**
1. 更换USB端口（建议使用USB 2.0端口，兼容性更好）
2. 更换USB线（确保支持数据传输，不是仅充电线）
3. 如果有多条USB线，尝试不同的线

**每次更换后，运行：**
```powershell
adb kill-server
adb start-server
adb devices
```

---

### 步骤5：检查Windows设备管理器

**操作：**
1. 按 `Win + X`
2. 选择"设备管理器"
3. 查看是否有：
   - "Android Phone" 或 "Android设备"
   - 带黄色感叹号的"未知设备"或"便携设备"

**结果：**
- ✅ 看到"Android Phone"（无感叹号） → 驱动正常，继续步骤6
- ⚠️ 看到带黄色感叹号的设备 → 需要安装驱动
- ❌ 什么都没有 → USB连接可能有问题

**如果看到黄色感叹号：**
- 需要安装手机厂商的USB驱动
- 或使用手机厂商的助手软件（会自动安装驱动）

---

### 步骤6：撤销并重新授权

**如果以上步骤都不行，尝试：**

1. **在手机上：**
   - 开发者选项 → "撤销USB调试授权"
   - 或点击"撤销所有USB调试授权"

2. **拔掉USB线，等待5秒**

3. **重新连接USB线**

4. **手机上应该弹出授权对话框：**
   - 勾选"始终允许这台计算机"
   - 点击"确定"

5. **在电脑上运行：**
   ```powershell
   adb kill-server
   adb start-server
   adb devices
   ```

---

## 测试命令

每完成一个步骤，运行以下命令测试：

```powershell
# 重启ADB服务
adb kill-server
adb start-server

# 检查设备
adb devices

# 检查设备详细信息
adb devices -l

# 获取设备状态
adb get-state
```

---

## 成功标志

当您看到以下输出时，说明连接成功：

```powershell
PS> adb devices
List of devices attached
XXXXXXXX    device
```

其中：
- `XXXXXXXX` 是设备序列号
- `device` 表示设备已连接并授权

---

## 如果仍然无法连接

### 方案A：使用WiFi连接

如果USB一直有问题，可以尝试WiFi连接（需要USB先连接一次）：

```powershell
# 1. 先用USB连接一次（如果可能）
adb devices

# 2. 启用WiFi调试
adb tcpip 5555

# 3. 在手机上查看WiFi IP地址
#    设置 → WLAN → 点击已连接的WiFi → 查看IP地址
#    假设IP是 192.168.1.100

# 4. 通过WiFi连接
adb connect 192.168.1.100:5555

# 5. 断开USB，验证连接
adb devices
```

### 方案B：使用Android Studio

1. 打开Android Studio
2. 连接手机
3. 如果Android Studio能识别设备，说明硬件没问题
4. 可能是ADB配置问题

### 方案C：检查其他电脑

1. 在另一台电脑上尝试连接
2. 如果在其他电脑上能连接，说明是当前电脑的问题
3. 如果在其他电脑上也不能连接，说明是手机或USB线的问题

---

## 连接成功后的下一步

一旦连接成功，您可以：

### 1. 查看Logcat日志

```powershell
# 清除旧日志
adb logcat -c

# 查看过滤后的日志（查找10684发件人的短信）
adb logcat | Select-String -Pattern "SmsReader|10684|9-5-5038|菜鸟驿站"
```

### 2. 重新安装应用（如果需要）

```powershell
# Full版
adb install -r android/app/build/outputs/apk/full/release/sms-agent-fullRelease-1.3.0.apk
```

### 3. 查看应用信息

```powershell
# 查看已安装的应用
adb shell pm list packages | Select-String "sms.tagger"

# 查看应用详细信息
adb shell dumpsys package com.sms.tagger
```

---

## 快速参考

**最重要的三个步骤：**
1. ✅ USB连接模式选择"文件传输"
2. ✅ 启用USB调试并授权
3. ✅ 品牌特定设置已配置

**每完成一个步骤，都运行 `adb devices` 检查！**

---

需要我帮您检查具体是哪个步骤出了问题吗？


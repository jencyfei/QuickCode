# APK 安装指南

## 📱 APK 文件

**文件名**: `app-release-20251117-v3-final.apk`
**大小**: 10.96 MB
**版本**: 1.0.0
**最后更新**: 2025-11-17

---

## 🚀 安装方法

### 方法1: ADB 安装（推荐）

#### 前置条件
- 安装了 Android SDK Platform Tools
- 手机连接到电脑
- 手机启用了 USB 调试

#### 安装步骤
```bash
# 1. 打开命令行/PowerShell
# 2. 进入APK文件所在目录
cd d:\tools\python\mypro\sms_agent

# 3. 运行安装命令
adb install app-release-20251117-v3-final.apk

# 4. 等待安装完成
# 显示 "Success" 表示安装成功
```

#### 卸载旧版本（如需要）
```bash
adb uninstall com.sms.tagger
```

---

### 方法2: 手动安装

#### 步骤
1. **将APK文件复制到手机**
   - 连接手机到电脑
   - 使用文件管理器或ADB复制文件

2. **在手机上打开文件管理器**
   - 找到APK文件
   - 点击APK文件

3. **允许安装**
   - 如果提示"未知来源"，点击"设置"
   - 启用"允许来自此来源的应用"
   - 返回并点击安装

4. **等待安装完成**
   - 安装完成后点击"打开"或"完成"

---

### 方法3: 通过开发者选项安装

#### 前置条件
- 手机启用了开发者选项
- 手机启用了 USB 调试

#### 步骤
```bash
# 1. 连接手机到电脑
# 2. 打开命令行/PowerShell
# 3. 进入APK文件所在目录
cd d:\tools\python\mypro\sms_agent

# 4. 运行安装命令
adb install app-release-20251117-v3-final.apk

# 5. 手机会提示允许安装
# 6. 点击"允许"
# 7. 等待安装完成
```

---

## ✅ 验证安装

### 检查安装是否成功
```bash
adb shell pm list packages | findstr sms_tagger
```

如果输出显示 `com.sms.tagger`，说明安装成功。

### 启动应用
```bash
adb shell am start -n com.sms.tagger/.MainActivity
```

---

## 🧪 首次使用

### 1. 授予权限
应用首次启动时会请求以下权限：
- ✅ 读取短信
- ✅ 读取联系人
- ✅ 访问存储空间

**请点击"允许"以启用所有功能**

### 2. 加载短信
- 应用会自动加载你的短信
- 第一次加载可能需要几秒钟
- 之后会缓存数据，加载会更快

### 3. 开始使用
- 点击"快递"标签查看快递信息
- 点击"短信"标签查看短信列表
- 点击"标签"标签管理短信标签
- 点击"设置"进行应用设置

---

## 🔧 常见问题

### Q1: 安装失败，提示"命令不存在"
**A**: 需要安装 Android SDK Platform Tools
- 下载: https://developer.android.com/studio/releases/platform-tools
- 解压后将 `platform-tools` 目录添加到系统PATH

### Q2: 安装失败，提示"设备未找到"
**A**: 
- 检查手机是否连接到电脑
- 检查是否启用了 USB 调试
- 尝试重新连接手机

### Q3: 安装失败，提示"权限被拒绝"
**A**:
- 在手机上启用 USB 调试
- 在电脑上运行命令行时使用管理员权限

### Q4: 应用无法读取短信
**A**:
- 在手机设置中授予应用"读取短信"权限
- 设置 > 应用 > SMS Tagger > 权限 > 短信 > 允许

### Q5: 应用闪退
**A**:
- 尝试卸载并重新安装
- 清除应用数据: `adb shell pm clear com.sms.tagger`
- 重新启动手机

---

## 📊 系统要求

- **Android 版本**: 6.0 或更高
- **存储空间**: 最少 50 MB
- **RAM**: 最少 2 GB
- **权限**: 读取短信、读取联系人

---

## 🔄 更新和卸载

### 更新应用
```bash
# 直接安装新版本APK
adb install app-release-20251117-v3-final.apk
```

### 卸载应用
```bash
# 方法1: 使用ADB
adb uninstall com.sms.tagger

# 方法2: 在手机上
设置 > 应用 > SMS Tagger > 卸载
```

---

## 📞 获取帮助

如果遇到问题：

1. **查看日志**
```bash
adb logcat | grep sms_tagger
```

2. **清除缓存**
```bash
adb shell pm clear com.sms.tagger
```

3. **重新安装**
```bash
adb uninstall com.sms.tagger
adb install app-release-20251117-v3-final.apk
```

---

## ✨ 新功能

### 快递功能
- ✅ 按日期分组显示快递
- ✅ 同一天按取件码顺序排列
- ✅ 取件状态持久化保存

### 短信功能
- ✅ 显示所有短信（包括流量券等）
- ✅ 按标签分类
- ✅ 支持搜索和过滤

### 规则功能
- ✅ 支持自定义规则
- ✅ 规则配置持久化
- ✅ 支持启用/禁用规则

---

**准备好了吗？** 👉 立即安装并开始使用！


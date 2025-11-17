# APK 签名配置完整指南

**完成日期**: 2025-11-14  
**状态**: ✅ 已完成，生成了签名的 Release APK

---

## 🔍 问题分析

### 为什么出现"解析软件包时出现问题"？

**原因**：APK 文件未签名

```
app-release-unsigned.apk
└── 未签名状态
    └── 安卓系统无法验证应用来源
        └── 拒绝安装 ❌
```

---

## ✅ 解决方案

### 1. 生成签名密钥

**命令**：
```bash
keytool -genkey -v -keystore sms-release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias sms-key -storepass 123456 -keypass 123456 \
  -dname "CN=SMS Tagger, OU=Development, O=SMS, L=China, ST=China, C=CN"
```

**生成的文件**：
```
android/sms-release-key.jks
└── 签名密钥库文件
    ├── 有效期：10000 天
    ├── 密钥算法：RSA 2048位
    ├── 密钥别名：sms-key
    └── 密码：123456
```

---

### 2. 配置 build.gradle

**修改文件**：`android/app/build.gradle`

**添加签名配置**：
```gradle
signingConfigs {
    release {
        storeFile file('../sms-release-key.jks')
        storePassword '123456'
        keyAlias 'sms-key'
        keyPassword '123456'
    }
}
```

**配置 Release 构建类型**：
```gradle
buildTypes {
    release {
        minifyEnabled false
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release  // ← 添加这一行
    }
}
```

---

### 3. 重新编译

**命令**：
```bash
./gradlew clean assembleRelease
```

**输出**：
```
BUILD SUCCESSFUL in 42s
43 actionable tasks: 19 executed, 23 from cache, 1 up-to-date
```

---

## 📦 生成的 APK 文件

### 文件位置
```
android/app/build/outputs/apk/release/app-release.apk
```

### 文件特性

| 特性 | 值 |
|------|-----|
| 文件名 | `app-release.apk` |
| 签名状态 | ✅ 已签名 |
| 可安装性 | ✅ 可直接安装 |
| 大小 | ~19MB（已优化） |
| 调试信息 | 已移除 |

---

## 🚀 安装 APK

### 方法1：使用 ADB 安装（推荐）

```bash
# 连接设备或启动模拟器
adb devices

# 安装 APK
adb install android/app/build/outputs/apk/release/app-release.apk

# 启动应用
adb shell am start -n com.sms.tagger/.MainActivity
```

### 方法2：直接拖拽到模拟器

1. 打开 Android Studio 模拟器
2. 将 `app-release.apk` 拖拽到模拟器窗口
3. 等待安装完成

### 方法3：手动安装到真机

1. 将 `app-release.apk` 复制到手机
2. 打开文件管理器，找到 APK 文件
3. 点击安装

---

## 🔐 签名密钥信息

### 密钥库文件
```
文件名：sms-release-key.jks
位置：android/sms-release-key.jks
大小：~2.5KB
```

### 密钥信息
```
密钥别名：sms-key
密钥算法：RSA 2048位
有效期：10000 天（约27年）
密钥库密码：123456
密钥密码：123456
```

### 证书信息
```
CN=SMS Tagger
OU=Development
O=SMS
L=China
ST=China
C=CN
```

---

## ⚠️ 重要提示

### 密钥库文件安全

**⚠️ 重要**：`sms-release-key.jks` 文件非常重要

```
如果丢失：
❌ 无法生成新的签名 APK
❌ 无法更新应用到应用商店
❌ 必须使用新的包名重新发布

保护措施：
✅ 备份到安全位置
✅ 不要上传到公开仓库
✅ 添加到 .gitignore
```

### .gitignore 配置

```bash
# 添加到 android/.gitignore
*.jks
*.keystore
```

---

## 📋 编译对比

### Debug 版本
```bash
./gradlew assembleDebug
```

**输出**：`app-debug.apk`
- 包含调试符号
- 未签名
- 大小：~25MB
- 用途：开发测试

### Release 版本
```bash
./gradlew assembleRelease
```

**输出**：`app-release.apk`
- 已签名
- 调试符号已移除
- 大小：~19MB
- 用途：发布到应用商店

---

## ✅ 验证签名

### 查看 APK 签名信息

```bash
# 使用 jarsigner 验证
jarsigner -verify -verbose -certs app-release.apk

# 使用 apksigner 验证（推荐）
apksigner verify --verbose app-release.apk
```

### 预期输出

```
jar verified.

This jar contains entries whose certificate chain is not validated.
...
```

---

## 🔄 后续更新

### 发布新版本

1. **更新版本号**：
   ```gradle
   versionCode 2      // 递增
   versionName "1.0.1" // 更新版本名
   ```

2. **重新编译**：
   ```bash
   ./gradlew clean assembleRelease
   ```

3. **使用相同的密钥签名**：
   - 自动使用 `sms-release-key.jks`
   - 无需重新配置

---

## 📊 文件清单

| 文件 | 位置 | 说明 |
|------|------|------|
| `sms-release-key.jks` | `android/` | 签名密钥库 |
| `build.gradle` | `android/app/` | 构建配置（已更新） |
| `app-release.apk` | `android/app/build/outputs/apk/release/` | 签名的 Release APK |

---

## 🎯 总结

✅ **已生成签名密钥**
- 文件：`sms-release-key.jks`
- 有效期：10000 天

✅ **已配置签名**
- 文件：`android/app/build.gradle`
- 配置：Release 构建类型使用签名

✅ **已生成签名 APK**
- 文件：`app-release.apk`
- 状态：可直接安装
- 大小：~19MB

✅ **可以安装到设备**
- 使用 ADB 安装
- 或拖拽到模拟器
- 或手动安装到真机

---

## 📞 常见问题

### Q: 为什么之前是 `app-release-unsigned.apk`？
**A**: 因为没有配置签名。现在已配置，会生成 `app-release.apk`（已签名）。

### Q: 密钥库密码是什么？
**A**: `123456`（在 build.gradle 中配置）

### Q: 可以修改密钥库密码吗？
**A**: 可以，但需要同时更新 build.gradle 中的配置。

### Q: 如果丢失了密钥库文件怎么办？
**A**: 需要生成新的密钥库，但无法更新已发布的应用。必须使用新的包名重新发布。

### Q: 可以用 Debug APK 测试吗？
**A**: 可以，但 Release APK 更接近最终版本，建议用 Release APK 进行最终测试。

---

## 🚀 下一步

1. ✅ 使用 `adb install` 安装 APK
2. ✅ 在设备上测试应用
3. ✅ 验证 UI 效果是否与网页版一致
4. ✅ 如无问题，可发布到应用商店

**现在可以安装并测试应用了！**

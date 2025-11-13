# Android项目快速开始

## 🎯 一键构建APK

### 最简单的方式

```bash
cd android
build_apk.bat
```

等待构建完成后，APK文件在：
```
app\build\outputs\apk\debug\app-debug.apk
```

## 📋 前置要求

### 必需
- **Java JDK 8+**
  - 检查: `java -version`
  - 下载: https://www.oracle.com/java/technologies/downloads/

### 可选
- **Android SDK** (如果要用Android Studio开发)
  - 修改 `local.properties` 中的SDK路径

## 🚀 构建步骤

### 1. 首次构建

第一次构建会下载Gradle和依赖，需要较长时间（5-10分钟）：

```bash
cd d:\tools\python\mypro\sms_agent\android
build_apk.bat
```

### 2. 后续构建

已有缓存后，构建会很快（1-2分钟）：

```bash
build_apk.bat
```

## 📱 安装APK

### 方法1: 直接安装
1. 将 `app-debug.apk` 复制到手机
2. 在手机上打开APK文件
3. 允许安装未知来源应用

### 方法2: ADB安装
```bash
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 🔧 常见问题

### Q1: Gradle下载慢

**A**: 首次构建需要下载Gradle（约100MB），请耐心等待。

如果太慢，可以使用镜像：
编辑 `gradle/wrapper/gradle-wrapper.properties`，将URL改为：
```
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.0-bin.zip
```

### Q2: 找不到Java

**A**: 安装JDK并设置环境变量 `JAVA_HOME`

### Q3: SDK路径错误

**A**: 如果不用Android Studio开发，可以忽略这个警告。
如果要修改，编辑 `local.properties`。

### Q4: 构建失败

**A**: 
1. 检查网络连接
2. 删除 `.gradle` 文件夹后重试
3. 查看错误信息

## 📊 项目文件说明

```
android/
├── build_apk.bat          # 一键构建脚本 ⭐
├── build.gradle           # 项目配置
├── settings.gradle        # 项目设置
├── gradle.properties      # Gradle属性
├── gradlew.bat           # Gradle Wrapper
├── local.properties       # SDK路径
└── app/
    ├── build.gradle       # 应用配置
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/          # Kotlin代码
        └── res/           # 资源文件
```

## 🎨 修改应用

### 修改应用名称
编辑 `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">你的应用名</string>
```

### 修改版本号
编辑 `app/build.gradle`:
```gradle
versionCode 1
versionName "1.0.0"
```

### 修改主题颜色
编辑 `app/src/main/res/values/colors.xml`:
```xml
<color name="primary">#FF6B9D</color>
```

## 📚 详细文档

- **构建指南**: README_BUILD.md
- **开发计划**: ../docs/ANDROID_DEVELOPMENT_PLAN.md
- **项目说明**: README.md

## ✅ 验证构建

构建成功后，你应该看到：

```
BUILD SUCCESSFUL in 2m 15s
45 actionable tasks: 45 executed

APK location:
  app\build\outputs\apk\debug\app-debug.apk
```

现在可以安装APK到手机测试了！

---

**提示**: 第一次构建需要下载依赖，请确保网络畅通。

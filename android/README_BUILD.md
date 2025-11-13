# Android项目构建指南

## 📋 项目结构

```
android/
├── app/
│   ├── build.gradle                    # 应用级Gradle配置
│   ├── proguard-rules.pro             # 混淆规则
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # 应用清单
│           ├── java/com/sms/tagger/
│           │   ├── MainActivity.kt    # 主Activity
│           │   ├── ui/theme/          # 主题配置
│           │   ├── data/              # 数据层（已有）
│           │   └── util/              # 工具类（已有）
│           └── res/                   # 资源文件
│               ├── values/
│               │   ├── strings.xml
│               │   ├── colors.xml
│               │   └── themes.xml
│               └── xml/
├── gradle/
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle Wrapper配置
├── build.gradle                       # 项目级Gradle配置
├── settings.gradle                    # 项目设置
├── gradle.properties                  # Gradle属性
├── gradlew.bat                       # Gradle Wrapper (Windows)
├── local.properties                   # 本地SDK路径
└── build_apk.bat                     # 一键构建脚本
```

## 🚀 快速开始

### 前置要求

1. **Java JDK 8+**
   - 下载: https://www.oracle.com/java/technologies/downloads/
   - 配置环境变量 `JAVA_HOME`

2. **Android SDK** (可选，如果只是构建APK)
   - 如果已安装Android Studio，SDK路径通常在:
     - `C:\Users\[用户名]\AppData\Local\Android\Sdk`
   - 修改 `local.properties` 中的 `sdk.dir` 为你的SDK路径

### 方法1: 一键构建（推荐）

```bash
# 直接运行构建脚本
build_apk.bat
```

这会自动：
1. 检查Gradle Wrapper
2. 下载依赖
3. 编译项目
4. 生成APK

生成的APK位置：
```
app\build\outputs\apk\debug\app-debug.apk
```

### 方法2: 使用Gradle命令

```bash
# 构建Debug版本
gradlew.bat assembleDebug

# 构建Release版本
gradlew.bat assembleRelease

# 清理构建
gradlew.bat clean

# 安装到设备
gradlew.bat installDebug
```

## 📦 构建输出

### Debug APK
- 路径: `app/build/outputs/apk/debug/app-debug.apk`
- 特点: 包含调试信息，文件较大
- 用途: 开发测试

### Release APK
- 路径: `app/build/outputs/apk/release/app-release-unsigned.apk`
- 特点: 优化过的版本，需要签名
- 用途: 正式发布

## 🔧 配置说明

### 修改应用信息

编辑 `app/build.gradle`:

```gradle
android {
    defaultConfig {
        applicationId "com.sms.tagger"  // 应用包名
        versionCode 1                    // 版本号
        versionName "1.0.0"             // 版本名称
    }
}
```

### 修改应用名称

编辑 `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">短信助手</string>
```

### 修改主题颜色

编辑 `app/src/main/res/values/colors.xml`:

```xml
<color name="primary">#FF6B9D</color>
```

## 🐛 常见问题

### 1. Gradle下载慢

**问题**: Gradle下载速度很慢

**解决**: 使用国内镜像

编辑 `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://mirrors.cloud.tencent.com/gradle/gradle-8.0-bin.zip
```

或编辑 `build.gradle`:
```gradle
allprojects {
    repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
        google()
        mavenCentral()
    }
}
```

### 2. SDK路径错误

**问题**: `SDK location not found`

**解决**: 修改 `local.properties`:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

### 3. Java版本不对

**问题**: `Unsupported class file major version`

**解决**: 
1. 安装JDK 8或更高版本
2. 设置 `JAVA_HOME` 环境变量

### 4. 内存不足

**问题**: `OutOfMemoryError`

**解决**: 编辑 `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### 5. 依赖下载失败

**问题**: 某些依赖下载失败

**解决**: 
1. 检查网络连接
2. 使用VPN或镜像源
3. 删除 `.gradle` 缓存目录后重试

## 📱 安装APK

### 方法1: 通过USB

1. 启用开发者选项和USB调试
2. 连接手机到电脑
3. 运行: `gradlew.bat installDebug`

### 方法2: 手动安装

1. 将APK复制到手机
2. 在手机上打开APK文件
3. 允许安装未知来源应用

## 🔐 签名配置（Release版本）

### 1. 生成密钥库

```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### 2. 配置签名

在 `app/build.gradle` 中添加:

```gradle
android {
    signingConfigs {
        release {
            storeFile file("my-release-key.jks")
            storePassword "your-store-password"
            keyAlias "my-key-alias"
            keyPassword "your-key-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. 构建签名APK

```bash
gradlew.bat assembleRelease
```

## 📊 项目状态

- ✅ Gradle构建配置完成
- ✅ AndroidManifest配置完成
- ✅ 基础MainActivity完成
- ✅ 主题和资源文件完成
- ✅ 数据层代码完成（已有）
- ✅ 工具类完成（已有）
- ⏳ UI页面开发（下一步）
- ⏳ ViewModel开发
- ⏳ 完整功能实现

## 🔗 相关链接

- **后端API**: http://localhost:10043/
- **Android开发指南**: ../docs/ANDROID_DEVELOPMENT_PLAN.md
- **项目README**: README.md

## 💡 下一步

1. **测试构建**:
   ```bash
   build_apk.bat
   ```

2. **安装测试**:
   - 将生成的APK安装到手机
   - 检查应用是否正常启动

3. **开发UI**:
   - 创建登录页面
   - 创建短信列表页面
   - 创建标签管理页面

4. **实现功能**:
   - 短信读取
   - API集成
   - 数据同步

---

**创建时间**: 2025-11-10  
**状态**: ✅ 构建环境已配置完成  
**下一步**: 运行 `build_apk.bat` 测试构建

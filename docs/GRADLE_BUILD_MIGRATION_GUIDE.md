# Gradle 打包配置迁移指南

## 概述

本文档说明如何将当前项目的 Gradle 打包配置（包括 Trial/Full 双版本构建）迁移到新的 Android 项目中。

## 需要复制的文件

### 1. 核心配置文件（必须）

#### 1.1 根目录 `build.gradle`
**位置**: `android/build.gradle`

**关键内容**:
- Kotlin 版本配置
- Android Gradle Plugin 版本
- 构建脚本依赖

**需要修改的地方**:
- 根据新项目的 Kotlin 版本调整 `kotlin_version`
- 根据新项目的 Android Gradle Plugin 版本调整

#### 1.2 应用模块 `app/build.gradle`
**位置**: `android/app/build.gradle`

**关键配置块**:

```gradle
// 签名配置
signingConfigs {
    release {
        storeFile file('../your-release-key.jks')  // 修改为你的签名文件路径
        storePassword 'your_password'              // 修改为你的密码
        keyAlias 'your_alias'                      // 修改为你的别名
        keyPassword 'your_password'                 // 修改为你的密码
    }
}

// 构建类型
buildTypes {
    release {
        minifyEnabled false
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        signingConfig signingConfigs.release
    }
}

// APK 命名规则
applicationVariants.all { variant ->
    variant.outputs.all { output ->
        if (output.outputFileName != null && output.outputFileName.endsWith(".apk")) {
            def baseName = "your-app-name-${variant.name}-${variant.versionName}.apk"
            output.outputFileName = baseName
        }
    }
}

// Product Flavors（核心配置）
flavorDimensions += "version"
productFlavors {
    full {
        dimension "version"
        buildConfigField "boolean", "IS_TRIAL", "false"
    }
    trial {
        dimension "version"
        applicationIdSuffix ".trial"
        versionNameSuffix "-trial"
        buildConfigField "boolean", "IS_TRIAL", "true"
    }
}

// BuildConfig 支持（必须）
buildFeatures {
    compose true  // 如果使用 Compose
    buildConfig true  // 必须启用，用于 IS_TRIAL 标志
}
```

**需要修改的地方**:
- `namespace` 和 `applicationId` - 改为新项目的包名
- `signingConfigs` - 使用新项目的签名配置
- APK 命名规则中的 `your-app-name` - 改为新项目名称
- 根据新项目需求调整其他配置

#### 1.3 `settings.gradle`
**位置**: `android/settings.gradle`

**关键内容**:
- 仓库配置
- 项目名称

**需要修改的地方**:
- `rootProject.name` - 改为新项目名称

#### 1.4 `gradle.properties`
**位置**: `android/gradle.properties`

**关键配置**:
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true

android.useAndroidX=true
android.enableJetifier=true

kotlin.code.style=official
```

**说明**: 这些是通用的 Gradle 优化配置，可以直接使用。

#### 1.5 `gradle-wrapper.properties`
**位置**: `android/gradle/wrapper/gradle-wrapper.properties`

**关键内容**:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
```

**说明**: 确保新项目使用相同或兼容的 Gradle 版本。

### 2. 签名文件（可选，但推荐）

#### 2.1 签名密钥文件
**位置**: `android/sms-release-key.jks`（或你的签名文件）

**说明**:
- 如果新项目已有签名文件，使用新项目的
- 如果没有，可以复制当前项目的（**注意安全**）
- 或者生成新的签名文件

**生成新签名文件命令**:
```bash
keytool -genkey -v -keystore your-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias your-alias
```

### 3. Gradle Wrapper 文件（推荐）

**文件列表**:
- `gradlew` / `gradlew.bat` - Gradle 包装脚本
- `gradle/wrapper/gradle-wrapper.jar` - Wrapper JAR 文件
- `gradle/wrapper/gradle-wrapper.properties` - Wrapper 配置

**说明**: 确保新项目使用相同的 Gradle 版本，避免版本冲突。

## 迁移步骤

### 步骤 1: 复制配置文件

1. **复制核心配置文件**:
   ```bash
   # 从当前项目复制
   cp android/build.gradle <新项目>/build.gradle
   cp android/app/build.gradle <新项目>/app/build.gradle
   cp android/settings.gradle <新项目>/settings.gradle
   cp android/gradle.properties <新项目>/gradle.properties
   cp android/gradle/wrapper/gradle-wrapper.properties <新项目>/gradle/wrapper/gradle-wrapper.properties
   ```

2. **复制 Gradle Wrapper**（如果新项目没有）:
   ```bash
   cp -r android/gradle <新项目>/
   cp android/gradlew <新项目>/
   cp android/gradlew.bat <新项目>/
   ```

### 步骤 2: 修改配置适配新项目

#### 2.1 修改 `app/build.gradle`

**必须修改的配置**:

1. **包名和命名空间**:
   ```gradle
   namespace 'com.your.new.package'  // 改为新项目的包名
   applicationId "com.your.new.package"  // 改为新项目的包名
   ```

2. **签名配置**:
   ```gradle
   signingConfigs {
       release {
           storeFile file('../your-release-key.jks')  // 改为新项目的签名文件
           storePassword 'your_password'              // 改为新项目的密码
           keyAlias 'your_alias'                     // 改为新项目的别名
           keyPassword 'your_password'                // 改为新项目的密码
       }
   }
   ```

3. **APK 命名规则**:
   ```gradle
   applicationVariants.all { variant ->
       variant.outputs.all { output ->
           if (output.outputFileName != null && output.outputFileName.endsWith(".apk")) {
               def baseName = "your-new-app-name-${variant.name}-${variant.versionName}.apk"
               output.outputFileName = baseName
           }
       }
   }
   ```

4. **版本信息**:
   ```gradle
   versionCode 1  // 根据新项目调整
   versionName "1.0.0"  // 根据新项目调整
   ```

#### 2.2 修改 `settings.gradle`

```gradle
rootProject.name = "YourNewProjectName"  // 改为新项目名称
include ':app'
```

### 步骤 3: 在代码中使用 BuildConfig.IS_TRIAL

在新项目的代码中，可以通过 `BuildConfig.IS_TRIAL` 来判断当前构建的是 Trial 版还是 Full 版：

```kotlin
if (BuildConfig.IS_TRIAL) {
    // Trial 版逻辑
} else {
    // Full 版逻辑
}
```

### 步骤 4: 验证配置

1. **同步 Gradle**:
   ```bash
   cd <新项目>
   ./gradlew tasks
   ```

2. **检查构建变体**:
   应该能看到以下构建变体：
   - `fullDebug`
   - `fullRelease`
   - `trialDebug`
   - `trialRelease`

3. **测试打包**:
   ```bash
   # 打包 Trial Release 版本
   ./gradlew assembleTrialRelease
   
   # 打包 Full Release 版本
   ./gradlew assembleFullRelease
   ```

4. **验证 APK**:
   - 检查 APK 文件是否生成在 `app/build/outputs/apk/` 目录
   - 检查 APK 文件名是否符合命名规则
   - 安装 APK 验证 `BuildConfig.IS_TRIAL` 是否正确

## 关键配置说明

### Product Flavors 工作原理

1. **Full 版本**:
   - 包名: `com.your.package`
   - 版本名: `1.0.0`
   - `BuildConfig.IS_TRIAL = false`

2. **Trial 版本**:
   - 包名: `com.your.package.trial`
   - 版本名: `1.0.0-trial`
   - `BuildConfig.IS_TRIAL = true`

### 构建命令

```bash
# 构建所有变体
./gradlew assemble

# 构建特定变体
./gradlew assembleTrialRelease    # Trial Release
./gradlew assembleTrialDebug      # Trial Debug
./gradlew assembleFullRelease     # Full Release
./gradlew assembleFullDebug       # Full Debug

# 安装到设备
./gradlew installTrialRelease
./gradlew installFullRelease
```

## 常见问题

### Q1: 构建失败，提示找不到签名文件
**A**: 检查 `signingConfigs` 中的 `storeFile` 路径是否正确，确保签名文件存在。

### Q2: BuildConfig.IS_TRIAL 无法使用
**A**: 确保在 `buildFeatures` 中启用了 `buildConfig true`。

### Q3: 两个版本的 APK 包名相同
**A**: 检查 `trial` flavor 中是否设置了 `applicationIdSuffix ".trial"`。

### Q4: Gradle 版本不兼容
**A**: 确保新项目的 Android Gradle Plugin 版本与 Gradle 版本兼容。参考 [官方兼容性表格](https://developer.android.com/studio/releases/gradle-plugin)。

## 文件清单总结

### 必须复制的文件
- [x] `build.gradle` (根目录)
- [x] `app/build.gradle` (应用模块)
- [x] `settings.gradle`
- [x] `gradle.properties`
- [x] `gradle/wrapper/gradle-wrapper.properties`

### 推荐复制的文件
- [ ] `gradlew` / `gradlew.bat`
- [ ] `gradle/wrapper/gradle-wrapper.jar`

### 可选文件
- [ ] 签名密钥文件（如果使用相同的签名）

## 注意事项

1. **安全性**: 签名密钥文件包含敏感信息，不要提交到版本控制系统
2. **版本兼容性**: 确保新项目的 Android SDK 版本、Kotlin 版本等与配置兼容
3. **依赖冲突**: 新项目可能已有不同的依赖版本，需要解决冲突
4. **测试**: 迁移后务必测试所有构建变体，确保都能正常打包和运行

## 参考

- [Android Gradle Plugin 官方文档](https://developer.android.com/studio/build)
- [Product Flavors 文档](https://developer.android.com/studio/build/build-variants)
- [BuildConfig 文档](https://developer.android.com/reference/android/os/Build)


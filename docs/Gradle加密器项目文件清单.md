# Gradle 加密器项目文件复制清单

## 目标
在新项目 `D:\tools\python\mypro\jmq` 中创建一个独立的 Gradle 项目，用于打包加密器功能。

## 项目结构

创建以下目录结构：
```
jmq/
├── encryptor/                    # 加密器模块（新建）
│   ├── src/
│   │   └── main/
│   │       └── kotlin/
│   │           └── com/
│   │               └── jmq/
│   │                   └── encryptor/
│   │                       └── FernetDecryptor.kt
│   └── build.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle                  # 根构建文件
├── settings.gradle
├── gradle.properties
├── gradlew.bat                   # Windows 脚本
├── gradlew                       # Unix 脚本（可选）
└── .gitignore
```

---

## 必须复制的文件

### 1. Gradle 配置文件

#### 根目录构建文件 `build.gradle`
```gradle
// Top-level build file
buildscript {
    ext.kotlin_version = '1.9.10'
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version"
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

#### 设置文件 `settings.gradle`
```gradle
rootProject.name = "jmq-encryptor"
include ':encryptor'
```

#### Gradle 属性文件 `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
kotlin.code.style=official
```

#### Gradle Wrapper 文件
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle/wrapper/gradle-wrapper.jar`
- `gradlew.bat`（Windows）
- `gradlew`（Unix/Linux/Mac，可选）

---

### 2. 加密器模块

#### 模块构建文件 `encryptor/build.gradle`
```gradle
plugins {
    id 'java-library'
    id 'org.jetbrains.kotlin.jvm'
}

group = 'com.jmq'
version = '1.0.0'

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

kotlin {
    jvmTarget = '1.8'
}

dependencies {
    // Kotlin 标准库
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin_version"
    
    // 测试
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.jetbrains.kotlin:kotlin-test-junit'
}

// 打包配置
jar {
    archiveBaseName = 'jmq-encryptor'
    archiveVersion = version
    manifest {
        attributes(
            'Implementation-Title': project.name,
            'Implementation-Version': version
        )
    }
}
```

#### 加密器源代码 `encryptor/src/main/kotlin/com/jmq/encryptor/FernetDecryptor.kt`

需要从 Android 项目复制并修改：

1. **移除 Android 依赖**：
   - 将 `android.util.Base64` 替换为 Java 标准库的 Base64

2. **修改后的代码**：
```kotlin
package com.jmq.encryptor

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Fernet 兼容解密器（纯 Java/Kotlin 实现）
 * 
 * Fernet 格式（128 字节 token）:
 * - Version (1 byte): 0x80
 * - Timestamp (8 bytes): big-endian uint64
 * - IV (16 bytes): AES-CBC 初始化向量
 * - Ciphertext (variable): AES-128-CBC 加密的数据（PKCS7 填充）
 * - HMAC (32 bytes): SHA256 HMAC 签名
 * 
 * Fernet Key (32 bytes base64 encoded):
 * - Signing key (16 bytes): 用于 HMAC
 * - Encryption key (16 bytes): 用于 AES
 */
object FernetDecryptor {

    private const val VERSION: Byte = 0x80.toByte()
    private const val VERSION_SIZE = 1
    private const val TIMESTAMP_SIZE = 8
    private const val IV_SIZE = 16
    private const val HMAC_SIZE = 32
    private const val KEY_SIZE = 16

    /**
     * 解密 Fernet token
     * 
     * @param fernetKey Base64 编码的 Fernet 密钥（32 字节解码后）
     * @param token Fernet token（双层 urlsafe base64 编码）
     * @return 解密后的明文字符串
     * @throws IllegalArgumentException 如果格式错误或验证失败
     */
    @JvmStatic
    fun decrypt(fernetKey: String, token: String): String {
        // 使用 Java 标准库的 Base64
        val base64Decoder = Base64.getUrlDecoder()
        
        // 1. 解码 Fernet 密钥
        val keyBytes = base64Decoder.decode(fernetKey.trim())
        if (keyBytes.size != 32) {
            throw IllegalArgumentException("Invalid Fernet key length: ${keyBytes.size}, expected 32")
        }
        val signingKey = keyBytes.copyOfRange(0, KEY_SIZE)
        val encryptionKey = keyBytes.copyOfRange(KEY_SIZE, 32)

        // 2. 解码 token（双层 base64 解码）
        val tokenBytes = try {
            val fernetTokenBytes = base64Decoder.decode(token.trim())
            base64Decoder.decode(fernetTokenBytes)
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to decode token: ${e.message}")
        }

        // 3. 验证最小长度
        val minLength = VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE + HMAC_SIZE
        if (tokenBytes.size < minLength) {
            throw IllegalArgumentException("Token too short: ${tokenBytes.size}, minimum $minLength")
        }

        // 4. 解析 token 结构
        val version = tokenBytes[0]
        if (version != VERSION) {
            throw IllegalArgumentException("Invalid Fernet version: $version, expected $VERSION")
        }

        val timestamp = ByteBuffer.wrap(tokenBytes, VERSION_SIZE, TIMESTAMP_SIZE)
            .order(ByteOrder.BIG_ENDIAN)
            .long

        val iv = tokenBytes.copyOfRange(VERSION_SIZE + TIMESTAMP_SIZE, VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE)
        
        val ciphertextStart = VERSION_SIZE + TIMESTAMP_SIZE + IV_SIZE
        val ciphertextEnd = tokenBytes.size - HMAC_SIZE
        val ciphertext = tokenBytes.copyOfRange(ciphertextStart, ciphertextEnd)
        
        val hmacReceived = tokenBytes.copyOfRange(ciphertextEnd, tokenBytes.size)

        // 5. 验证 HMAC
        val dataToSign = tokenBytes.copyOfRange(0, ciphertextEnd)
        val hmacExpected = computeHmac(signingKey, dataToSign)
        if (!hmacExpected.contentEquals(hmacReceived)) {
            throw IllegalArgumentException("HMAC verification failed")
        }

        // 6. AES-128-CBC 解密
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKey = SecretKeySpec(encryptionKey, "AES")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
        
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }

    private fun computeHmac(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }
}
```

---

### 3. Gradle Wrapper 文件

从 Android 项目复制以下文件：

- ✅ `android/gradle/wrapper/gradle-wrapper.properties` → `gradle/wrapper/gradle-wrapper.properties`
- ✅ `android/gradle/wrapper/gradle-wrapper.jar` → `gradle/wrapper/gradle-wrapper.jar`
- ✅ `android/gradlew.bat` → `gradlew.bat`
- ✅ `android/gradlew` → `gradlew`（可选，如果需要在 Linux/Mac 上使用）

---

## 文件清单总结

| 文件类型 | 来源路径 | 目标路径 | 状态 |
|---------|---------|---------|------|
| **Gradle 配置文件** |
| `build.gradle` | 新建（基于 android/build.gradle 简化） | `jmq/build.gradle` | ⚠️ 需要修改 |
| `settings.gradle` | 新建 | `jmq/settings.gradle` | ⚠️ 需要修改 |
| `gradle.properties` | `android/gradle.properties` | `jmq/gradle.properties` | ✅ 可直接复制 |
| **Gradle Wrapper** |
| `gradle-wrapper.properties` | `android/gradle/wrapper/` | `jmq/gradle/wrapper/` | ✅ 可直接复制 |
| `gradle-wrapper.jar` | `android/gradle/wrapper/` | `jmq/gradle/wrapper/` | ✅ 可直接复制 |
| `gradlew.bat` | `android/` | `jmq/` | ✅ 可直接复制 |
| `gradlew` | `android/` | `jmq/` | ⭕ 可选 |
| **源代码** |
| `FernetDecryptor.kt` | `android/app/src/main/java/com/sms/tagger/util/` | `jmq/encryptor/src/main/kotlin/com/jmq/encryptor/` | ⚠️ 需要修改 |
| `encryptor/build.gradle` | 新建 | `jmq/encryptor/build.gradle` | ⚠️ 需要新建 |

---

## 复制步骤

### 步骤 1: 创建目录结构
```bash
cd D:\tools\python\mypro\jmq
mkdir -p encryptor\src\main\kotlin\com\jmq\encryptor
mkdir -p gradle\wrapper
```

### 步骤 2: 复制 Gradle Wrapper 文件
```bash
# 从 Android 项目复制
copy D:\tools\python\mypro\sms_agent\android\gradle\wrapper\*.* D:\tools\python\mypro\jmq\gradle\wrapper\
copy D:\tools\python\mypro\sms_agent\android\gradlew.bat D:\tools\python\mypro\jmq\
```

### 步骤 3: 创建/复制 Gradle 配置文件
- 创建简化版的 `build.gradle`（见上方示例）
- 创建 `settings.gradle`（见上方示例）
- 复制 `gradle.properties`

### 步骤 4: 复制并修改加密器代码
- 复制 `FernetDecryptor.kt`
- 修改包名和 Base64 导入

### 步骤 5: 创建模块构建文件
- 创建 `encryptor/build.gradle`（见上方示例）

---

## 构建和使用

### 构建 JAR
```bash
cd D:\tools\python\mypro\jmq
.\gradlew.bat encryptor:build
```

生成的 JAR 文件位置：
- `encryptor/build/libs/jmq-encryptor-1.0.0.jar`
- `encryptor/build/libs/jmq-encryptor-1.0.0-sources.jar`（源代码）
- `encryptor/build/libs/jmq-encryptor-1.0.0-javadoc.jar`（文档）

### 在 Python 中使用（通过 Jython 或调用 Java）

### 在其他 Java/Kotlin 项目中使用

将生成的 JAR 添加到依赖：
```gradle
dependencies {
    implementation files('path/to/jmq-encryptor-1.0.0.jar')
}
```

---

## 注意事项

1. **Base64 替换**: Android 的 `android.util.Base64` 需要替换为 Java 标准库的 `java.util.Base64`
2. **包名修改**: 建议修改包名为 `com.jmq.encryptor`
3. **依赖简化**: 移除所有 Android 相关依赖
4. **Java 版本**: 确保兼容 Java 8+

---

## 可选：添加加密器（如果也需要生成功能）

如果需要添加加密功能（对应 Python 的生成功能），可以添加 `FernetEncryptor.kt`：

```kotlin
package com.jmq.encryptor

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import kotlin.random.Random

object FernetEncryptor {
    // 实现加密功能（对应 Python 的 generate_activation_code）
    // ...
}
```


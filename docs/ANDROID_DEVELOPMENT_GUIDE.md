# Android App 开发指南

> **项目名称**: 短信标签助手 (Smart SMS Tagger)  
> **开发语言**: Kotlin  
> **UI框架**: Jetpack Compose  
> **最低SDK**: Android 6.0 (API 23)  
> **目标SDK**: Android 14 (API 34)

---

## 📋 开发环境准备

### 1. 安装Android Studio

下载并安装最新版本的Android Studio：
- 官网: https://developer.android.com/studio
- 推荐版本: Android Studio Hedgehog (2023.1.1) 或更高

### 2. 配置SDK

在Android Studio中配置SDK：
1. 打开 **Settings** → **Appearance & Behavior** → **System Settings** → **Android SDK**
2. 安装以下组件：
   - Android SDK Platform 34 (Android 14)
   - Android SDK Platform-Tools
   - Android SDK Build-Tools
   - Android Emulator

### 3. 创建虚拟设备（可选）

如果没有实体设备，创建AVD（Android Virtual Device）：
1. 打开 **Tools** → **Device Manager**
2. 点击 **Create Device**
3. 选择设备型号（推荐：Pixel 6）
4. 选择系统镜像（推荐：Android 14, API 34）

---

## 🚀 创建Android项目

### 方法1: 使用Android Studio创建

1. 打开Android Studio
2. 选择 **New Project**
3. 选择 **Empty Activity** (Compose)
4. 配置项目：
   ```
   Name: SmsTagger
   Package name: com.sms.tagger
   Save location: D:\tools\python\mypro\sms_agent\android
   Language: Kotlin
   Minimum SDK: API 23 (Android 6.0)
   Build configuration language: Kotlin DSL (build.gradle.kts)
   ```
5. 点击 **Finish**

### 方法2: 使用命令行创建（高级）

```bash
cd D:\tools\python\mypro\sms_agent
mkdir android
cd android

# 使用Gradle初始化项目
gradle init --type kotlin-application
```

---

## 📦 项目结构

创建完成后，项目结构如下：

```
android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sms/tagger/
│   │   │   │   ├── MainActivity.kt          # 主Activity
│   │   │   │   ├── ui/                      # UI层
│   │   │   │   │   ├── screens/             # 页面
│   │   │   │   │   │   ├── SplashScreen.kt
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── SmsListScreen.kt
│   │   │   │   │   │   ├── SmsDetailScreen.kt
│   │   │   │   │   │   ├── TagManageScreen.kt
│   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   ├── components/          # 可复用组件
│   │   │   │   │   │   ├── SmsCard.kt
│   │   │   │   │   │   ├── TagChip.kt
│   │   │   │   │   │   └── EmptyState.kt
│   │   │   │   │   └── theme/               # 主题配置
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   ├── data/                    # 数据层
│   │   │   │   │   ├── model/               # 数据模型
│   │   │   │   │   │   ├── User.kt
│   │   │   │   │   │   ├── Sms.kt
│   │   │   │   │   │   └── Tag.kt
│   │   │   │   │   ├── repository/          # 数据仓库
│   │   │   │   │   │   ├── SmsRepository.kt
│   │   │   │   │   │   ├── TagRepository.kt
│   │   │   │   │   │   └── UserRepository.kt
│   │   │   │   │   ├── local/               # 本地数据库
│   │   │   │   │   │   ├── SmsDao.kt
│   │   │   │   │   │   ├── TagDao.kt
│   │   │   │   │   │   └── AppDatabase.kt
│   │   │   │   │   └── remote/              # 网络API
│   │   │   │   │       ├── ApiService.kt
│   │   │   │   │       ├── AuthApi.kt
│   │   │   │   │       ├── SmsApi.kt
│   │   │   │   │       └── TagApi.kt
│   │   │   │   ├── viewmodel/               # ViewModel层
│   │   │   │   │   ├── SmsViewModel.kt
│   │   │   │   │   ├── TagViewModel.kt
│   │   │   │   │   └── AuthViewModel.kt
│   │   │   │   ├── util/                    # 工具类
│   │   │   │   │   ├── SmsReader.kt         # 短信读取
│   │   │   │   │   ├── SmsReceiver.kt       # 短信监听
│   │   │   │   │   └── PreferencesManager.kt
│   │   │   │   └── di/                      # 依赖注入
│   │   │   │       └── AppModule.kt
│   │   │   ├── AndroidManifest.xml          # 清单文件
│   │   │   └── res/                         # 资源文件
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   └── colors.xml
│   │   │       └── drawable/
│   │   └── test/                            # 测试
│   ├── build.gradle.kts                     # 应用级构建配置
│   └── proguard-rules.pro                   # 混淆规则
├── build.gradle.kts                         # 项目级构建配置
├── settings.gradle.kts                      # 项目设置
└── gradle.properties                        # Gradle属性
```

---

## 🔧 配置依赖

编辑 `app/build.gradle.kts`，添加必要的依赖：

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.sms.tagger"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sms.tagger"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Activity Compose
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Retrofit (网络请求)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Room (本地数据库)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")
    
    // Hilt (依赖注入)
    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // DataStore (数据存储)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // Coil (图片加载)
    implementation("io.coil-kt:coil-compose:2.5.0")
    
    // Accompanist (Compose扩展)
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## 📱 配置权限

编辑 `app/src/main/AndroidManifest.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- 网络权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <!-- 短信权限 -->
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    
    <!-- 通知权限 (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:name=".SmsTaggerApplication"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.SmsTagger"
        android:usesCleartextTraffic="true"
        tools:targetApi="31">
        
        <!-- 主Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.SmsTagger">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- 短信接收器 -->
        <receiver
            android:name=".util.SmsReceiver"
            android:exported="true"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>
        
    </application>

</manifest>
```

---

## 🎨 配置主题

### 1. 定义颜色 (`res/values/colors.xml`)

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 粉色系主题 -->
    <color name="primary">#FF6B9D</color>
    <color name="primary_light">#FF8FAB</color>
    <color name="primary_dark">#E5608C</color>
    
    <color name="background">#FFF5F5</color>
    <color name="surface">#FFFFFF</color>
    <color name="text_primary">#333333</color>
    <color name="text_secondary">#666666</color>
    
    <color name="black">#000000</color>
    <color name="white">#FFFFFF</color>
</resources>
```

### 2. 配置Compose主题 (`ui/theme/Color.kt`)

```kotlin
package com.sms.tagger.ui.theme

import androidx.compose.ui.graphics.Color

val Primary = Color(0xFFFF6B9D)
val PrimaryLight = Color(0xFFFF8FAB)
val PrimaryDark = Color(0xFFE5608C)

val Background = Color(0xFFFFF5F5)
val Surface = Color(0xFFFFFFFF)
val TextPrimary = Color(0xFF333333)
val TextSecondary = Color(0xFF666666)
```

---

## 🔌 配置API服务

### 1. 创建API接口 (`data/remote/ApiService.kt`)

```kotlin
package com.sms.tagger.data.remote

import com.sms.tagger.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // 认证相关
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<User>
    
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // 标签相关
    @GET("api/tags")
    suspend fun getTags(): Response<TagListResponse>
    
    @POST("api/tags")
    suspend fun createTag(@Body tag: TagCreate): Response<Tag>
    
    @PUT("api/tags/{id}")
    suspend fun updateTag(@Path("id") id: Int, @Body tag: TagUpdate): Response<Tag>
    
    @DELETE("api/tags/{id}")
    suspend fun deleteTag(@Path("id") id: Int): Response<Unit>
    
    // 短信相关
    @GET("api/sms")
    suspend fun getSmsList(
        @Query("keyword") keyword: String? = null,
        @Query("tag_ids") tagIds: String? = null,
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): Response<SmsListResponse>
    
    @POST("api/sms")
    suspend fun createSms(@Body sms: SmsCreate): Response<Sms>
    
    @POST("api/sms/batch")
    suspend fun createSmsBatch(@Body request: SmsBatchCreate): Response<List<Sms>>
    
    @POST("api/sms/{id}/tags")
    suspend fun addTagsToSms(
        @Path("id") smsId: Int,
        @Body request: SmsAddTags
    ): Response<Sms>
    
    @POST("api/sms/batch-add-tags")
    suspend fun batchAddTags(@Body request: SmsBatchAddTags): Response<BatchResult>
    
    @POST("api/sms/batch-delete")
    suspend fun batchDeleteSms(@Body request: SmsBatchDelete): Response<Unit>
}
```

### 2. 配置Retrofit (`di/AppModule.kt`)

```kotlin
package com.sms.tagger.di

import com.sms.tagger.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    private const val BASE_URL = "http://10.0.2.2:8000/"  // Android模拟器访问本机
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
```

---

## 📝 下一步开发任务

### 阶段1: 基础功能（1周）
1. ✅ 创建项目结构
2. ✅ 配置依赖和权限
3. ⏳ 实现短信读取功能
4. ⏳ 实现网络请求层
5. ⏳ 实现本地数据库

### 阶段2: UI开发（1周）
1. ⏳ 启动页和登录页
2. ⏳ 短信列表页
3. ⏳ 短信详情页
4. ⏳ 标签管理页
5. ⏳ 设置页

### 阶段3: 功能完善（1周）
1. ⏳ 自动同步功能
2. ⏳ 后台服务
3. ⏳ 通知功能
4. ⏳ 性能优化

---

## 🔗 相关资源

- **Android官方文档**: https://developer.android.com/docs
- **Jetpack Compose教程**: https://developer.android.com/jetpack/compose/tutorial
- **Kotlin官方文档**: https://kotlinlang.org/docs/home.html
- **后端API文档**: http://localhost:8000/docs

---

**创建时间**: 2025-11-05  
**状态**: 准备开始Android开发 🚀

# Gradle 加密器项目文件复制脚本
# 目标：在新项目中创建独立的 Gradle 加密器模块

$sourceDir = "D:\tools\python\mypro\sms_agent"
$targetDir = "D:\tools\python\mypro\jmq"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Gradle 加密器项目 - 文件复制脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 创建目录结构
Write-Host "创建目录结构..." -ForegroundColor Yellow
$dirsToCreate = @(
    "$targetDir\encryptor\src\main\kotlin\com\jmq\encryptor",
    "$targetDir\encryptor\src\test\kotlin\com\jmq\encryptor",
    "$targetDir\gradle\wrapper"
)

foreach ($dir in $dirsToCreate) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}
Write-Host "✓ 目录结构创建完成" -ForegroundColor Green
Write-Host ""

# 复制 Gradle Wrapper 文件
Write-Host "复制 Gradle Wrapper 文件..." -ForegroundColor Yellow
$wrapperFiles = @(
    @{
        Source = "$sourceDir\android\gradle\wrapper\gradle-wrapper.properties"
        Target = "$targetDir\gradle\wrapper\gradle-wrapper.properties"
        Name = "gradle-wrapper.properties"
    },
    @{
        Source = "$sourceDir\android\gradle\wrapper\gradle-wrapper.jar"
        Target = "$targetDir\gradle\wrapper\gradle-wrapper.jar"
        Name = "gradle-wrapper.jar"
    },
    @{
        Source = "$sourceDir\android\gradlew.bat"
        Target = "$targetDir\gradlew.bat"
        Name = "gradlew.bat"
    }
)

foreach ($file in $wrapperFiles) {
    if (Test-Path $file.Source) {
        Copy-Item -Path $file.Source -Destination $file.Target -Force
        Write-Host "  ✓ $($file.Name)" -ForegroundColor Green
    } else {
        Write-Host "  ⚠ $($file.Name) (源文件不存在)" -ForegroundColor Yellow
    }
}
Write-Host ""

# 复制 gradle.properties
Write-Host "复制 Gradle 配置文件..." -ForegroundColor Yellow
if (Test-Path "$sourceDir\android\gradle.properties") {
    Copy-Item -Path "$sourceDir\android\gradle.properties" -Destination "$targetDir\gradle.properties" -Force
    Write-Host "  ✓ gradle.properties" -ForegroundColor Green
} else {
    Write-Host "  ⚠ gradle.properties (源文件不存在)" -ForegroundColor Yellow
}
Write-Host ""

# 创建根目录 build.gradle
Write-Host "创建根目录 build.gradle..." -ForegroundColor Yellow
$rootBuildGradle = @"
// Top-level build file
buildscript {
    ext.kotlin_version = '1.9.10'
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:`$kotlin_version"
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
"@
$rootBuildGradle | Out-File -FilePath "$targetDir\build.gradle" -Encoding UTF8
Write-Host "  ✓ build.gradle" -ForegroundColor Green
Write-Host ""

# 创建 settings.gradle
Write-Host "创建 settings.gradle..." -ForegroundColor Yellow
$settingsGradle = @"
rootProject.name = "jmq-encryptor"
include ':encryptor'
"@
$settingsGradle | Out-File -FilePath "$targetDir\settings.gradle" -Encoding UTF8
Write-Host "  ✓ settings.gradle" -ForegroundColor Green
Write-Host ""

# 创建 encryptor 模块的 build.gradle
Write-Host "创建 encryptor 模块 build.gradle..." -ForegroundColor Yellow
$moduleBuildGradle = @"
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
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.10"
    
    // 测试
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.jetbrains.kotlin:kotlin-test-junit:1.9.10'
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
"@
$moduleBuildGradle | Out-File -FilePath "$targetDir\encryptor\build.gradle" -Encoding UTF8
Write-Host "  ✓ encryptor/build.gradle" -ForegroundColor Green
Write-Host ""

# 复制并修改 FernetDecryptor.kt
Write-Host "复制并修改 FernetDecryptor.kt..." -ForegroundColor Yellow
$decryptorSource = "$sourceDir\android\app\src\main\java\com\sms\tagger\util\FernetDecryptor.kt"
$decryptorTarget = "$targetDir\encryptor\src\main\kotlin\com\jmq\encryptor\FernetDecryptor.kt"

if (Test-Path $decryptorSource) {
    $content = Get-Content -Path $decryptorSource -Raw -Encoding UTF8
    
    # 替换包名
    $content = $content -replace "package com\.sms\.tagger\.util", "package com.jmq.encryptor"
    
    # 替换 Android Base64 为 Java Base64
    $content = $content -replace "import android\.util\.Base64", "import java.util.Base64"
    
    # 添加 @JvmStatic 注解到 decrypt 方法
    $content = $content -replace "(\s+)fun decrypt\(", "`$1@JvmStatic`n`$1fun decrypt("
    
    # 替换 Base64.decode 调用
    $content = $content -replace 'Base64\.decode\(([^,]+),\s*Base64\.URL_SAFE\s*\|\s*Base64\.NO_WRAP\)', 'Base64.getUrlDecoder().decode($1)'
    
    # 保存修改后的文件
    $content | Out-File -FilePath $decryptorTarget -Encoding UTF8 -NoNewline
    Write-Host "  ✓ FernetDecryptor.kt (已修改包名和Base64导入)" -ForegroundColor Green
} else {
    Write-Host "  ✗ FernetDecryptor.kt (源文件不存在)" -ForegroundColor Red
}
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "✅ 文件复制和创建完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "下一步：" -ForegroundColor Yellow
Write-Host "1. 检查并手动修改 FernetDecryptor.kt（确保 Base64 替换正确）" -ForegroundColor Cyan
Write-Host "2. 测试构建: cd $targetDir && .\gradlew.bat encryptor:build" -ForegroundColor Cyan
Write-Host "3. 生成的 JAR: encryptor\build\libs\jmq-encryptor-1.0.0.jar" -ForegroundColor Cyan
Write-Host ""


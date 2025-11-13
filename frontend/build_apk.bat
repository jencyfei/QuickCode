@echo off
echo ========================================
echo 构建 SMS Agent APK
echo ========================================
echo.

echo [1/3] 构建 Web 应用...
call npm run build
if errorlevel 1 (
    echo 构建失败！
    pause
    exit /b 1
)

echo.
echo [2/3] 同步到 Android 项目...
call npx cap sync
if errorlevel 1 (
    echo 同步失败！
    pause
    exit /b 1
)

echo.
echo [3/3] 构建 APK...
cd android
call gradlew.bat assembleDebug --no-daemon --offline
if errorlevel 1 (
    echo 尝试在线构建...
    call gradlew.bat assembleDebug --no-daemon
)

if errorlevel 1 (
    echo APK 构建失败！
    echo.
    echo 可能的原因：
    echo 1. 未安装 Android SDK
    echo 2. 网络问题导致 Gradle 下载失败
    echo 3. Java 版本不兼容
    echo.
    echo 请检查以上问题后重试
    cd ..
    pause
    exit /b 1
)

cd ..

echo.
echo ========================================
echo 构建成功！
echo ========================================
echo.
echo APK 位置：
echo frontend\android\app\build\outputs\apk\debug\app-debug.apk
echo.
pause

@echo off
echo ========================================
echo   构建 Capacitor Web APK
echo ========================================
echo.

cd /d "%~dp0"

echo [1/5] 构建前端 Web 应用...
cd frontend
call npm run build
if errorlevel 1 (
    echo 前端构建失败！
    cd ..
    pause
    exit /b 1
)

echo.
echo [2/5] 同步到 Capacitor Android 项目...
call npx cap sync android
if errorlevel 1 (
    echo 同步失败！
    cd ..
    pause
    exit /b 1
)

echo.
echo [3/5] 修复 Gradle 版本兼容性...
cd android

REM 修改 capacitor-cordova-android-plugins 的 build.gradle
if exist "capacitor-cordova-android-plugins\build.gradle" (
    powershell -Command "(Get-Content 'capacitor-cordova-android-plugins\build.gradle') -replace '8\.7\.2', '8.1.2' | Set-Content 'capacitor-cordova-android-plugins\build.gradle'"
)

echo.
echo [4/5] 清理旧的构建...
call gradlew.bat clean --no-daemon

echo.
echo [5/5] 构建 APK...
call gradlew.bat assembleDebug --no-daemon

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   构建成功！
    echo ========================================
    echo.
    echo APK 位置：
    echo   frontend\android\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 安装到手机：
    echo   adb install -r frontend\android\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ========================================
    echo   构建失败！
    echo ========================================
    echo.
)

cd ..\..
pause

@echo off
echo ========================================
echo   SMS Agent - 构建 Web APK
echo ========================================
echo.

cd /d "%~dp0"

echo [1/4] 构建前端 Web 应用...
cd frontend
call npm run build
if errorlevel 1 (
    echo 前端构建失败！
    cd ..
    pause
    exit /b 1
)
cd ..

echo.
echo [2/4] 清理旧的 Web 资源...
if exist "android\app\src\main\assets\public" (
    rmdir /s /q "android\app\src\main\assets\public"
)

echo.
echo [3/4] 复制 Web 资源到 Android 项目...
mkdir "android\app\src\main\assets\public" 2>nul
xcopy /E /I /Y "frontend\dist\*" "android\app\src\main\assets\public\"
if errorlevel 1 (
    echo 复制文件失败！
    pause
    exit /b 1
)

echo.
echo [4/4] 构建 APK...
cd android
call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   构建成功！
    echo ========================================
    echo.
    echo APK 位置：
    echo   android\app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo 安装到手机：
    echo   adb install -r android\app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ========================================
    echo   构建失败！
    echo ========================================
    echo.
)

cd ..
pause

@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo   SMS Agent - 完整APK打包脚本
echo ========================================
echo.

:: 获取项目根目录
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

:: 颜色定义
set "GREEN=[92m"
set "RED=[91m"
set "YELLOW=[93m"
set "RESET=[0m"

echo.
echo [1] 检查环境...
echo.

:: 检查Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo %RED%错误: 未找到Node.js%RESET%
    echo 请从 https://nodejs.org 下载安装
    pause
    exit /b 1
)
echo ✓ Node.js 已安装

:: 检查Java
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%错误: 未找到Java%RESET%
    echo 请设置JAVA_HOME环境变量
    pause
    exit /b 1
)
echo ✓ Java 已安装

:: 检查Android SDK
if not defined ANDROID_HOME (
    echo %RED%错误: 未设置ANDROID_HOME环境变量%RESET%
    echo 请设置ANDROID_HOME指向Android SDK目录
    pause
    exit /b 1
)
echo ✓ Android SDK 已配置

echo.
echo [2] 构建前端...
echo.

cd /d "%PROJECT_ROOT%\frontend"

:: 检查node_modules
if not exist "node_modules" (
    echo 安装依赖...
    call npm install
    if errorlevel 1 (
        echo %RED%错误: npm install 失败%RESET%
        pause
        exit /b 1
    )
)

echo 构建前端...
call npm run build
if errorlevel 1 (
    echo %RED%错误: 前端构建失败%RESET%
    pause
    exit /b 1
)
echo ✓ 前端构建成功

echo.
echo [3] 同步到Android项目...
echo.

:: 检查dist目录
if not exist "dist" (
    echo %RED%错误: dist目录不存在%RESET%
    pause
    exit /b 1
)

:: 复制dist到android/app/src/main/assets/public
set ASSETS_DIR=%PROJECT_ROOT%\android\app\src\main\assets\public
if exist "%ASSETS_DIR%" (
    echo 清理旧文件...
    rmdir /s /q "%ASSETS_DIR%"
)

echo 复制新文件...
mkdir "%ASSETS_DIR%"
xcopy /e /i /y "dist\*" "%ASSETS_DIR%\"
if errorlevel 1 (
    echo %RED%错误: 文件复制失败%RESET%
    pause
    exit /b 1
)
echo ✓ 文件同步成功

echo.
echo [4] 构建APK...
echo.

cd /d "%PROJECT_ROOT%\android"

echo 执行Gradle构建...
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo %RED%错误: Gradle构建失败%RESET%
    echo 尝试Debug版本...
    call gradlew.bat assembleDebug
    if errorlevel 1 (
        echo %RED%错误: Debug版本构建也失败%RESET%
        pause
        exit /b 1
    )
    set APK_PATH=%PROJECT_ROOT%\android\app\build\outputs\apk\debug\app-debug.apk
) else (
    set APK_PATH=%PROJECT_ROOT%\android\app\build\outputs\apk\release\app-release.apk
)

if not exist "%APK_PATH%" (
    echo %RED%错误: APK文件未生成%RESET%
    pause
    exit /b 1
)

echo ✓ APK构建成功

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo APK文件位置:
echo   %APK_PATH%
echo.
echo 文件大小:
for %%A in ("%APK_PATH%") do echo   %%~zA 字节
echo.
echo 下一步:
echo   1. 连接Android设备到电脑
echo   2. 启用USB调试
echo   3. 运行: adb install "%APK_PATH%"
echo   或者直接将APK文件复制到手机安装
echo.

pause

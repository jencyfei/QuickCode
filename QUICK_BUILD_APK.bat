@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo   SMS Agent - 快速打包APK
echo ========================================
echo.

set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

echo [1] 构建前端...
cd /d "%PROJECT_ROOT%\frontend"
call npm run build
if errorlevel 1 (
    echo 前端构建失败
    pause
    exit /b 1
)
echo ✓ 前端构建成功

echo.
echo [2] 同步文件...
set ASSETS_DIR=%PROJECT_ROOT%\android\app\src\main\assets\public
if exist "%ASSETS_DIR%" rmdir /s /q "%ASSETS_DIR%"
mkdir "%ASSETS_DIR%"
xcopy /e /i /y "dist\*" "%ASSETS_DIR%\"
echo ✓ 文件同步成功

echo.
echo [3] 构建APK...
cd /d "%PROJECT_ROOT%\android"
call gradlew.bat assembleRelease
if errorlevel 1 (
    echo Release构建失败，尝试Debug...
    call gradlew.bat assembleDebug
)

echo.
echo ========================================
echo   打包完成！
echo ========================================
echo.
echo APK文件位置:
echo   android\app\build\outputs\apk\release\app-release.apk
echo   或
echo   android\app\build\outputs\apk\debug\app-debug.apk
echo.

pause

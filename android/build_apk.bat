@echo off
echo ========================================
echo   SMS Agent - Build APK
echo ========================================
echo.

cd /d "%~dp0"

echo Checking Gradle wrapper...
if not exist gradlew.bat (
    echo ERROR: gradlew.bat not found
    pause
    exit /b 1
)

echo.
echo Building APK...
echo.

call gradlew.bat assembleDebug

if %errorlevel% equ 0 (
    echo.
    echo ========================================
    echo   Build Success!
    echo ========================================
    echo.
    echo APK location:
    echo   app\build\outputs\apk\debug\app-debug.apk
    echo.
) else (
    echo.
    echo ========================================
    echo   Build Failed!
    echo ========================================
    echo.
)

pause

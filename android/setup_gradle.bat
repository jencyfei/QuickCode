@echo off
echo ========================================
echo   Setup Gradle Wrapper
echo ========================================
echo.

cd /d "%~dp0"

echo Creating gradle wrapper directory...
if not exist gradle\wrapper mkdir gradle\wrapper

echo.
echo Downloading gradle-wrapper.jar...
echo This may take a few minutes...
echo.

powershell -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://raw.githubusercontent.com/gradle/gradle/master/gradle/wrapper/gradle-wrapper.jar' -OutFile 'gradle/wrapper/gradle-wrapper.jar'}"

if exist gradle\wrapper\gradle-wrapper.jar (
    echo.
    echo ========================================
    echo   Setup Complete!
    echo ========================================
    echo.
    echo gradle-wrapper.jar downloaded successfully
    echo.
    echo Now you can run: build_apk.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   Setup Failed!
    echo ========================================
    echo.
    echo Failed to download gradle-wrapper.jar
    echo.
    echo Please check your internet connection
    echo.
)

pause

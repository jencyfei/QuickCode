@echo off
echo ========================================
echo   SMS Agent - Stop Services
echo ========================================
echo.

echo Stopping all services...
echo.

REM Stop Node processes
echo [1/2] Stopping frontend (Node.js)...
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Stopped all Node.js processes
) else (
    echo   INFO - No Node.js processes running
)
echo.

REM Stop Python processes
echo [2/2] Stopping backend (Python)...
taskkill /F /IM python.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Stopped all Python processes
) else (
    echo   INFO - No Python processes running
)
echo.

echo ========================================
echo   All services stopped
echo ========================================
echo.
pause

@echo off
echo ========================================
echo   SMS Agent - Restart Services
echo ========================================
echo.

REM Set project root directory
set PROJECT_ROOT=%~dp0
cd /d "%PROJECT_ROOT%"

echo [1/4] Stopping all services...
echo.

REM Stop Node processes
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Stopped Node.js processes
) else (
    echo   INFO - No Node.js processes running
)

REM Stop Python processes
taskkill /F /IM python.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo   OK - Stopped Python processes
) else (
    echo   INFO - No Python processes running
)

echo.
echo [2/4] Waiting for processes to end...
timeout /t 2 /nobreak >nul
echo   OK - Done
echo.

echo [3/4] Starting backend service...
start "SMS Agent - Backend" cmd /k "cd /d "%PROJECT_ROOT%backend" && echo Starting backend... && python -m uvicorn app.main:app --reload --port 10043"
echo   OK - Backend command executed
echo   Port: 10043
echo.

echo [4/4] Waiting for backend to start...
timeout /t 5 /nobreak >nul
echo   OK - Done
echo.

echo Starting frontend service...
start "SMS Agent - Frontend" cmd /k "cd /d "%PROJECT_ROOT%frontend" && echo Starting frontend... && npm run dev"
echo   OK - Frontend command executed
echo   Port: 3000 (or auto-selected)
echo.

echo ========================================
echo   Services restarted!
echo ========================================
echo.
echo Access URLs:
echo    Frontend: http://localhost:3000/
echo    Backend:  http://localhost:10043/docs
echo.
echo Notes:
echo    - Two service windows opened
echo    - Wait a few seconds for services to start
echo    - If port 3000 is busy, frontend will use another port
echo.
pause

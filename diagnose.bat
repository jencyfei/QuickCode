@echo off
echo ========================================
echo   SMS Agent - Service Diagnosis
echo ========================================
echo.

echo [1] Checking backend service (port 10043)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:10043/docs
if %errorlevel% equ 0 (
    echo   OK - Backend is responding
) else (
    echo   ERROR - Backend is not responding
)
echo.

echo [2] Checking frontend service (port 3000)...
curl -s -o nul -w "HTTP Status: %%{http_code}\n" http://localhost:3000/
if %errorlevel% equ 0 (
    echo   OK - Frontend is responding
) else (
    echo   ERROR - Frontend is not responding
)
echo.

echo [3] Checking processes...
echo.
echo Node.js processes:
tasklist | findstr node.exe
echo.
echo Python processes:
tasklist | findstr python.exe
echo.

echo [4] Checking ports...
echo.
echo Port 10043 (Backend):
netstat -ano | findstr :10043
echo.
echo Port 3000 (Frontend):
netstat -ano | findstr :3000
echo.

echo ========================================
echo   Diagnosis complete
echo ========================================
echo.
pause

@echo off
chcp 65001 >nul
echo ========================================
echo   SMS Agent - 环境配置向导
echo ========================================
echo.

cd /d "%~dp0backend"

REM 检查 .env 文件是否存在
if exist ".env" (
    echo ⚠️  .env 文件已存在
    echo.
    choice /C YN /M "是否覆盖现有配置"
    if errorlevel 2 (
        echo 已取消
        pause
        exit /b
    )
)

echo [1/5] 检查 PostgreSQL 服务...
echo.

REM 检查 PostgreSQL 服务
sc query postgresql-x64-14 >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ 未找到 PostgreSQL 服务
    echo.
    echo 请确保 PostgreSQL 已安装并运行
    echo 服务名称可能不同，请手动检查
    echo.
    pause
    exit /b 1
)

for /f "tokens=3" %%a in ('sc query postgresql-x64-14 ^| findstr "STATE"') do (
    if "%%a"=="RUNNING" (
        echo ✅ PostgreSQL 服务正在运行
    ) else (
        echo ⚠️  PostgreSQL 服务未运行
        echo.
        choice /C YN /M "是否启动 PostgreSQL 服务"
        if errorlevel 2 (
            echo 已取消
            pause
            exit /b
        )
        net start postgresql-x64-14
    )
)
echo.

echo [2/5] 配置数据库连接...
echo.

REM 获取数据库密码
set /p DB_PASSWORD="请输入 PostgreSQL 密码 (默认: postgres): "
if "%DB_PASSWORD%"=="" set DB_PASSWORD=postgres

REM 获取数据库名称
set /p DB_NAME="请输入数据库名称 (默认: sms_agent): "
if "%DB_NAME%"=="" set DB_NAME=sms_agent

echo.
echo [3/5] 测试数据库连接...
echo.

REM 测试数据库是否存在
psql -U postgres -d %DB_NAME% -c "SELECT 1" >nul 2>&1
if %errorlevel% neq 0 (
    echo ⚠️  数据库 %DB_NAME% 不存在
    echo.
    choice /C YN /M "是否创建数据库"
    if errorlevel 2 (
        echo 已取消
        pause
        exit /b
    )
    
    echo 正在创建数据库...
    psql -U postgres -c "CREATE DATABASE %DB_NAME%;" >nul 2>&1
    if %errorlevel% equ 0 (
        echo ✅ 数据库创建成功
    ) else (
        echo ❌ 数据库创建失败
        echo 请手动创建数据库: CREATE DATABASE %DB_NAME%;
        pause
        exit /b 1
    )
)
echo ✅ 数据库连接测试通过
echo.

echo [4/5] 生成配置文件...
echo.

REM 生成随机 SECRET_KEY
for /f %%i in ('python -c "import secrets; print(secrets.token_hex(32))"') do set SECRET_KEY=%%i

REM 创建 .env 文件
(
echo # 数据库配置
echo DATABASE_URL=postgresql://postgres:%DB_PASSWORD%@localhost:5432/%DB_NAME%
echo.
echo # JWT配置
echo SECRET_KEY=%SECRET_KEY%
echo ALGORITHM=HS256
echo ACCESS_TOKEN_EXPIRE_MINUTES=30
echo.
echo # 应用配置
echo APP_NAME=Smart SMS Tagger
echo DEBUG=True
echo ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
echo.
echo # 日志配置
echo LOG_LEVEL=INFO
) > .env

echo ✅ 配置文件已创建: backend\.env
echo.

echo [5/5] 验证配置...
echo.

REM 创建临时测试脚本
(
echo from app.config import settings
echo from sqlalchemy import create_engine, text
echo.
echo try:
echo     engine = create_engine(settings.DATABASE_URL^)
echo     with engine.connect(^) as conn:
echo         result = conn.execute(text("SELECT version()"^)^)
echo         print("✅ 数据库连接成功!"^)
echo except Exception as e:
echo     print("❌ 数据库连接失败:", e^)
echo     exit(1^)
) > test_config.py

REM 运行测试
python test_config.py
set TEST_RESULT=%errorlevel%

REM 删除测试脚本
del test_config.py

if %TEST_RESULT% neq 0 (
    echo.
    echo ❌ 配置验证失败
    echo 请检查数据库密码和连接信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo   🎉 配置完成！
echo ========================================
echo.
echo 配置信息:
echo   数据库: %DB_NAME%
echo   用户: postgres
echo   主机: localhost:5432
echo.
echo 下一步:
echo   1. 运行 start_all.bat 启动服务
echo   2. 访问 http://localhost:3000/
echo.
pause

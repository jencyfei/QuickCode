@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo   推送代码到 GitHub
echo ========================================
echo.

cd /d "%~dp0"

echo [1] 检查 Git 状态...
git status --short
echo.

echo [2] 添加修改的文件...
git add -A
echo ✓ 文件已添加

echo.
echo [3] 创建提交...
set /p COMMIT_MSG="请输入提交信息 (默认: Update code and fix issues): "
if "!COMMIT_MSG!"=="" set COMMIT_MSG=Update code and fix issues on 2025-11-17

git commit -m "!COMMIT_MSG!"
if errorlevel 1 (
    echo 没有新的更改需要提交
) else (
    echo ✓ 提交成功
)

echo.
echo [4] 推送到 GitHub...
git push origin main
if errorlevel 1 (
    echo 尝试推送到 master...
    git push origin master
    if errorlevel 1 (
        echo 错误: 推送失败
        pause
        exit /b 1
    )
)

echo.
echo ========================================
echo   推送完成！
echo ========================================
echo.
echo 仓库地址: https://github.com/jencyfei/sms_agent.git
echo.

pause

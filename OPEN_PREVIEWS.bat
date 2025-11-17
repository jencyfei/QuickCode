@echo off
chcp 65001 > nul
setlocal enabledelayedexpansion

echo ========================================
echo   打开所有页面预览
echo ========================================
echo.

cd /d "%~dp0"

echo 正在打开预览文件...
echo.

start preview_express_screen.html
timeout /t 1 /nobreak > nul

start preview_sms_list_screen.html
timeout /t 1 /nobreak > nul

start preview_tag_manage_screen.html
timeout /t 1 /nobreak > nul

start preview_rule_manage_screen.html
timeout /t 1 /nobreak > nul

start preview_settings_screen.html
timeout /t 1 /nobreak > nul

echo ✓ 所有预览文件已打开
echo.
echo 请在浏览器中检查每个页面的内容
echo 确认无问题后，在 PAGE_PREVIEW_GUIDE.md 中标记
echo.
echo 预览文件列表：
echo   1. preview_express_screen.html - 快递取件码
echo   2. preview_sms_list_screen.html - 短信列表
echo   3. preview_tag_manage_screen.html - 标签管理
echo   4. preview_rule_manage_screen.html - 自定义规则
echo   5. preview_settings_screen.html - 设置
echo.
pause

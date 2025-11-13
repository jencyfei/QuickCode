@echo off
echo ========================================
echo SMS Agent - Project Cleanup
echo ========================================
echo.
echo WARNING: This will delete outdated files
echo.
echo Press any key to continue or Ctrl+C to cancel
pause >nul
echo.
echo Starting cleanup...
echo.

echo [1/8] Removing duplicate startup scripts...
del /Q scripts\start_backend.bat 2>nul
del /Q scripts\start_backend_10043.bat 2>nul
del /Q scripts\start_backend_for_all.bat 2>nul
del /Q scripts\start_backend_simple.bat 2>nul
del /Q scripts\start_frontend.bat 2>nul
del /Q scripts\start_frontend_for_all.bat 2>nul
echo OK

echo [2/8] Removing outdated diagnostic scripts...
del /Q scripts\diagnose_and_fix.py 2>nul
del /Q scripts\diagnose_startup.py 2>nul
del /Q scripts\quick_fix.py 2>nul
echo OK

echo [3/8] Removing database reset scripts...
del /Q scripts\recreate_db.py 2>nul
del /Q scripts\recreate_db_auto.py 2>nul
del /Q scripts\reset_db.py 2>nul
echo OK

echo [4/8] Removing HTML preview files...
del /Q docs\android_preview_part1.html 2>nul
del /Q docs\android_preview_part2.html 2>nul
del /Q docs\android_styles.css 2>nul
del /Q docs\express_detail_page.html 2>nul
del /Q docs\gemini_tag.html 2>nul
del /Q docs\settings_with_theme.html 2>nul
del /Q docs\time_filter_page.html 2>nul
echo OK

echo [5/8] Removing outdated debug documents...
del /Q docs\TAG_COUNT_DEBUG.md 2>nul
del /Q docs\TAG_COUNT_ISSUE_FIX.md 2>nul
del /Q docs\TAG_COUNT_ISSUE_FINAL_FIX.md 2>nul
del /Q docs\TAG_CLICK_DEBUG.md 2>nul
del /Q docs\TAG_CLICK_NO_RESPONSE_FIX.md 2>nul
del /Q docs\DISPLAY_FIX_V2.md 2>nul
del /Q docs\EXPRESS_DETAIL_PAGE_FIX.md 2>nul
del /Q docs\FIX_EXPRESS_TAG_ISSUE.md 2>nul
del /Q docs\SMS_LIST_TAG_FILTER_FIX.md 2>nul
del /Q docs\UI_FIXES.md 2>nul
echo OK

echo [6/8] Removing stage documents...
del /Q docs\STAGE1_SUMMARY.md 2>nul
del /Q docs\STAGE2_SUMMARY.md 2>nul
del /Q docs\CURRENT_IMPLEMENTATION_PLAN.md 2>nul
echo OK

echo [7/8] Removing duplicate documents...
del /Q docs\QUICK_START.md 2>nul
del /Q docs\PAGE_FREEZE_FIX.md 2>nul
del /Q docs\EXPRESS_DETAIL_ANALYSIS.md 2>nul
echo OK

echo [8/8] Removing temporary files...
del /Q 1.task.md 2>nul
del /Q QUICK_FIX.md 2>nul
echo OK

rmdir /Q .specstory 2>nul

echo.
echo ========================================
echo Cleanup Complete
echo ========================================
echo.
echo Removed approximately 37 files
echo.
pause

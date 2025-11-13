@echo off
echo ========================================
echo SMS Agent - Cleanup Phase 2
echo ========================================
echo.
echo This will delete 10 outdated documents
echo.
echo Press any key to continue or Ctrl+C to cancel
pause >nul
echo.

echo Removing files...
del /Q docs\DESIGN_COMPARISON.md 2>nul
del /Q docs\DEVELOPMENT_OPTIONS.md 2>nul
del /Q docs\WEB_MOBILE_SOLUTION.md 2>nul
del /Q docs\TWO_LINE_LAYOUT.md 2>nul
del /Q docs\SMS_IMPORT_ANALYSIS.md 2>nul
del /Q docs\BANK_SMS_EXAMPLES.md 2>nul
del /Q docs\SMS_IMPORT_TEST_DATA.md 2>nul
del /Q docs\WEB_QUICK_START.md 2>nul

echo Removing Chinese filename files...
cd docs
del /Q "下一步优化.md" 2>nul
del /Q "EXPRESS_DETAIL_优化总结.md" 2>nul
cd ..

echo.
echo ========================================
echo Cleanup Complete
echo ========================================
echo.
echo Done
echo.
pause

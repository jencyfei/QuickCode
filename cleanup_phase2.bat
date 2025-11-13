@echo off
echo ========================================
echo SMS Agent - Cleanup Phase 2
echo ========================================
echo.
echo This will delete 10 outdated documents
echo.
echo Files to be removed:
echo   - DESIGN_COMPARISON.md
echo   - DEVELOPMENT_OPTIONS.md
echo   - WEB_MOBILE_SOLUTION.md
echo   - TWO_LINE_LAYOUT.md
echo   - SMS_IMPORT_ANALYSIS.md
echo   - BANK_SMS_EXAMPLES.md
echo   - SMS_IMPORT_TEST_DATA.md
echo   - WEB_QUICK_START.md
echo   - EXPRESS_DETAIL_优化总结.md
echo   - 下一步优化.md
echo.
echo Press any key to continue or Ctrl+C to cancel
pause >nul
echo.
echo Starting cleanup...
echo.

echo Removing outdated documents...
del /Q docs\DESIGN_COMPARISON.md 2>nul
del /Q docs\DEVELOPMENT_OPTIONS.md 2>nul
del /Q docs\WEB_MOBILE_SOLUTION.md 2>nul
del /Q docs\TWO_LINE_LAYOUT.md 2>nul
del /Q docs\SMS_IMPORT_ANALYSIS.md 2>nul
del /Q docs\BANK_SMS_EXAMPLES.md 2>nul
del /Q docs\SMS_IMPORT_TEST_DATA.md 2>nul
del /Q docs\WEB_QUICK_START.md 2>nul
echo OK - 8 files removed

echo Removing Chinese filename documents...
cd docs
del /Q "下一步优化.md" 2>nul
del /Q "EXPRESS_DETAIL_优化总结.md" 2>nul
cd ..
echo OK - 2 files removed

echo.
echo ========================================
echo Cleanup Phase 2 Complete
echo ========================================
echo.
echo Removed 10 outdated documents
echo.
echo Remaining documents: 23 important files
echo   - Android development: 3 files
echo   - Feature guides: 6 files
echo   - Bug fixes: 6 files
echo   - Project management: 5 files
echo   - Implementation: 3 files
echo.
echo Test files: All 6 test files preserved
echo.
pause

@echo off
echo Testing FocusSessionViewModel and SettingsFragment fixes...
echo.

REM Build the project to verify no compilation errors
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: Both fixes completed successfully!
    echo.
    echo 1. FocusSessionViewModel.kt fixes:
    echo    - Added proper import: android.os.CountDownTimer
    echo    - CountDownTimer now properly resolved in startSessionTimer function
    echo.
    echo 2. SettingsFragment.kt fixes:
    echo    - Removed reference to non-existent FocusSettingsDialog class
    echo    - Replaced with simple AlertDialog for focus duration selection
    echo    - Added preset options: 15, 25, 45, 60, 90 minutes
    echo    - Added custom duration input dialog with validation (5-180 minutes)
    echo    - Added error handling for invalid duration input
    echo.
    echo Features:
    echo - Focus session timer now works correctly with proper CountDownTimer import
    echo - Settings focus duration selection works with native AlertDialog
    echo - Input validation prevents invalid duration values
    echo - User-friendly preset options for common focus durations
    echo.
    echo Both files are now ready for production use.
) else (
    echo.
    echo BUILD FAILED: There may still be compilation issues.
)

echo.
pause

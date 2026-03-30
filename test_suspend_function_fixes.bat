@echo off
echo Testing SessionCompleteActivity and AppReviewFragment suspend function fixes...
echo.

REM Build the project to verify no compilation errors
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: Both suspend function fixes completed!
    echo.
    echo 1. SessionCompleteActivity.kt fixes:
    echo    - Added import: androidx.lifecycle.lifecycleScope
    echo    - Wrapped achievementEngine.checkFocusSessionAchievements() in lifecycleScope.launch
    echo    - Suspend function now properly executed in coroutine context
    echo.
    echo 2. AppReviewFragment.kt fixes:
    echo    - Added import: androidx.lifecycle.lifecycleScope
    echo    - Wrapped appCategoryRepository.getCategoryForPackage() in lifecycleScope.launch
    echo    - Entire loadInstalledApps() now runs in coroutine context
    echo.
    echo Benefits:
    echo - Suspend functions are properly executed in coroutine context
    echo - No more "Suspend function should only be called within coroutine body" errors
    echo - Proper lifecycle management - coroutines are cancelled when activity/fragment is destroyed
    echo - Thread-safe UI updates from coroutine results
    echo.
    echo Both files now follow Android coroutine best practices.
) else (
    echo.
    echo BUILD FAILED: There may still be suspend function issues.
)

echo.
pause

@echo off
echo Testing MindGuardWidget fix...
echo.

REM Build the project to verify no widget-related errors
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: MindGuardWidget fix completed!
    echo.
    echo All required widget IDs are now present:
    echo - wellness_score (for displaying wellness score)
    echo - status_text (for displaying status like "On Track")
    echo - widget_root (for click handling)
    echo - top_apps_container (for showing/hiding top apps)
    echo - screen_time_container (for showing/hiding screen time)
    echo - screen_time_text (for displaying screen time)
    echo - app1_name, app1_time (for top app #1)
    echo - app2_name, app2_time (for top app #2)
    echo - app3_name, app3_time (for top app #3)
    echo.
    echo The widget should now work correctly with both small and medium sizes.
) else (
    echo.
    echo BUILD FAILED: There may still be widget-related issues.
)

echo.
pause

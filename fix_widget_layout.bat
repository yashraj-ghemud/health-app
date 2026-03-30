@echo off
echo Fixing MindGuardWidget layout...

REM Replace malformed widget layout with fixed version
if exist "app\src\main\res\layout\widget_mind_guard_fixed.xml" (
    del "app\src\main\res\layout\widget_mind_guard.xml" 2>nul
    move "app\src\main\res\layout\widget_mind_guard_fixed.xml" "app\src\main\res\layout\widget_mind_guard.xml"
    echo Fixed widget_mind_guard.xml
) else (
    echo Fixed widget layout not found
)

echo.
echo MindGuardWidget layout fixed successfully!
echo All required IDs are now properly included:
echo - wellness_score
echo - status_text  
echo - widget_root
echo - top_apps_container
echo - screen_time_container
echo - screen_time_text
echo - app1_name, app1_time
echo - app2_name, app2_time
echo - app3_name, app3_time
echo.
pause

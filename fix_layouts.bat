@echo off
echo Fixing all ViewBinding layouts...

REM Replace malformed layouts with fixed versions
if exist "app\src\main\res\layout\fragment_onboarding_welcome_fixed.xml" (
    del "app\src\main\res\layout\fragment_onboarding_welcome.xml" 2>nul
    move "app\src\main\res\layout\fragment_onboarding_welcome_fixed.xml" "app\src\main\res\layout\fragment_onboarding_welcome.xml"
    echo Fixed fragment_onboarding_welcome.xml
)

if exist "app\src\main\res\layout\activity_onboarding_new.xml" (
    del "app\src\main\res\layout\activity_onboarding.xml" 2>nul
    move "app\src\main\res\layout\activity_onboarding_new.xml" "app\src\main\res\layout\activity_onboarding.xml"
    echo Fixed activity_onboarding.xml
)

if exist "app\src\main\res\layout\activity_focus_session_new.xml" (
    del "app\src\main\res\layout\activity_focus_session.xml" 2>nul
    move "app\src\main\res\layout\activity_focus_session_new.xml" "app\src\main\res\layout\activity_focus_session.xml"
    echo Fixed activity_focus_session.xml
)

if exist "app\src\main\res\layout\activity_main_new.xml" (
    del "app\src\main\res\layout\activity_main.xml" 2>nul
    move "app\src\main\res\layout\activity_main_new.xml" "app\src\main\res\layout\activity_main.xml"
    echo Fixed activity_main.xml
)

echo.
echo All layouts fixed successfully!
pause

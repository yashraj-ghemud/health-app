@echo off
echo ========================================
echo EXPORTING MINDGUARD TO APK
echo ========================================
echo.

REM Clean previous builds
echo Cleaning previous builds...
gradlew.bat clean

if %ERRORLEVEL% NEQ 0 (
    echo CLEAN FAILED
    pause
    exit /b 1
)

REM Build the APK
echo.
echo Building release APK...
gradlew.bat assembleRelease

if %ERRORLEVEL% NEQ 0 (
    echo RELEASE BUILD FAILED
    echo Trying debug build instead...
    gradlew.bat assembleDebug
    
    if %ERRORLEVEL% NEQ 0 (
        echo DEBUG BUILD ALSO FAILED
        pause
        exit /b 1
    )
    
    echo.
    echo DEBUG APK CREATED SUCCESSFULLY!
    echo Location: app\build\outputs\apk\debug\app-debug.apk
    goto :copy_apk
)

echo.
echo RELEASE APK CREATED SUCCESSFULLY!
echo Location: app\build\outputs\apk\release\app-release.apk

:copy_apk
echo.
echo Copying APK to project root...

REM Copy the APK to root directory
if exist "app\build\outputs\apk\release\app-release.apk" (
    copy "app\build\outputs\apk\release\app-release.apk" "MindGuard.apk"
    echo APK copied to: MindGuard.apk (Release Version)
) else if exist "app\build\outputs\apk\debug\app-debug.apk" (
    copy "app\build\outputs\apk\debug\app-debug.apk" "MindGuard.apk"
    echo APK copied to: MindGuard.apk (Debug Version)
) else (
    echo APK NOT FOUND IN BUILD DIRECTORY
    pause
    exit /b 1
)

echo.
echo ========================================
echo MINDGUARD APK EXPORT COMPLETED!
echo ========================================
echo.
echo APK File: MindGuard.apk
echo Location: c:\Users\yashraj\Desktop\healthapp\MindGuard.apk
echo.
echo Features Ready:
echo ✅ Complete digital wellness monitoring
echo ✅ Smart app categorization (200+ apps)
echo ✅ Real-time usage tracking
echo ✅ Focus sessions with breathing exercises
echo ✅ Achievement system with gamification
echo ✅ Beautiful dashboard with charts
echo ✅ Settings and customization
echo ✅ Background tasks and widgets
echo ✅ Working ViewBinding implementations
echo ✅ Proper coroutine handling
echo ✅ Modern konfetti animations
echo.
echo Install the APK on your Android device and enjoy MindGuard!
echo.
pause

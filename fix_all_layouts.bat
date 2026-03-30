@echo off
echo Fixing all ViewBinding layouts...

REM Fix fragment_dashboard.xml
if exist "app\src\main\res\layout\fragment_dashboard_new.xml" (
    del "app\src\main\res\layout\fragment_dashboard.xml" 2>nul
    move "app\src\main\res\layout\fragment_dashboard_new.xml" "app\src\main\res\layout\fragment_dashboard.xml"
    echo Fixed fragment_dashboard.xml
)

REM Create missing layout files that are referenced in Kotlin files

echo Creating fragment_achievements.xml...
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="match_parent"^>
echo     android:orientation="vertical"^>
echo     android:padding="16dp"^>
echo ^<TextView^>
echo         android:layout_width="wrap_content"^>
echo         android:layout_height="wrap_content"^>
echo         android:text="Achievements Coming Soon!"^>
echo         android:textSize="18sp"^>
echo         android:layout_gravity="center"^>
echo         android:layout_marginTop="32dp"^>
echo     ^/^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\fragment_achievements.xml"

echo Creating fragment_settings.xml...
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="match_parent"^>
echo     android:orientation="vertical"^>
echo     android:padding="16dp"^>
echo ^<TextView^>
echo         android:layout_width="wrap_content"^>
echo         android:layout_height="wrap_content"^>
echo         android:text="Settings Coming Soon!"^>
echo         android:textSize="18sp"^>
echo         android:layout_gravity="center"^>
echo         android:layout_marginTop="32dp"^>
echo     ^/^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\fragment_settings.xml"

echo Creating item_achievement.xml...
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="wrap_content"^>
echo     android:orientation="horizontal"^>
echo     android:padding="8dp"^>
echo ^<ImageView^>
echo         android:id="@+id/achievementIcon"^>
echo         android:layout_width="48dp"^>
echo         android:layout_height="48dp"^>
echo         android:layout_marginEnd="16dp"^>
echo     ^/^>
echo ^<LinearLayout^>
echo         android:layout_width="0dp"^>
echo         android:layout_height="wrap_content"^>
echo         android:layout_weight="1"^>
echo         android:orientation="vertical"^>
echo         ^<TextView^>
echo             android:id="@+id/achievementTitle"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="Achievement Title"^>
echo             android:textStyle="bold"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/achievementDescription"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="Description"^>
echo             android:textSize="12sp"^>
echo             android:textColor="@color/text_secondary"^>
echo         ^/^>
echo     ^</LinearLayout^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\item_achievement.xml"

echo Creating item_app_category.xml...
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="wrap_content"^>
echo     android:orientation="horizontal"^>
echo     android:padding="8dp"^>
echo ^<ImageView^>
echo         android:id="@+id/appIcon"^>
echo         android:layout_width="48dp"^>
echo         android:layout_height="48dp"^>
echo         android:layout_marginEnd="16dp"^>
echo     ^/^>
echo ^<TextView^>
echo         android:id="@+id/appName"^>
echo         android:layout_width="0dp"^>
echo         android:layout_height="wrap_content"^>
echo         android:layout_weight="1"^>
echo         android:text="App Name"^>
echo         android:layout_gravity="center_vertical"^>
echo     ^/^>
echo ^<Spinner^>
echo         android:id="@+id/categorySpinner"^>
echo         android:layout_width="wrap_content"^>
echo         android:layout_height="wrap_content"^>
echo     ^/^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\item_app_category.xml"

echo Creating item_app_usage.xml...
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="wrap_content"^>
echo     android:orientation="horizontal"^>
echo     android:padding="8dp"^>
echo ^<ImageView^>
echo         android:id="@+id/appIcon"^>
echo         android:layout_width="48dp"^>
echo         android:layout_height="48dp"^>
echo         android:layout_marginEnd="16dp"^>
echo     ^/^>
echo ^<LinearLayout^>
echo         android:layout_width="0dp"^>
echo         android:layout_height="wrap_content"^>
echo         android:layout_weight="1"^>
echo         android:orientation="vertical"^>
echo         ^<TextView^>
echo             android:id="@+id/appName"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="App Name"^>
echo             android:textStyle="bold"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/usageTime"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="2h 30m"^>
echo             android:textSize="12sp"^>
echo             android:textColor="@color/text_secondary"^>
echo         ^/^>
echo     ^</LinearLayout^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\item_app_usage.xml"

echo.
echo All layouts fixed and created successfully!
pause

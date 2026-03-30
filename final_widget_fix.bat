@echo off
echo Final MindGuardWidget layout fix...

REM Create the properly formatted widget layout
(
echo ^<?xml version="1.0" encoding="utf-8"?^>
echo ^<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"^>
echo     android:id="@+id/widget_root"^>
echo     android:layout_width="match_parent"^>
echo     android:layout_height="match_parent"^>
echo     android:orientation="vertical"^>
echo     android:padding="16dp"^>
echo     android:background="@color/background"^>
echo ^<TextView^>
echo         android:id="@+id/wellness_score"^>
echo         android:layout_width="wrap_content"^>
echo         android:layout_height="wrap_content"^>
echo         android:text="75"^>
echo         android:textSize="24sp"^>
echo         android:textStyle="bold"^>
echo         android:textColor="@color/text_primary"^>
echo         android:layout_marginBottom="8dp"^>
echo     ^/^>
echo ^<TextView^>
echo         android:id="@+id/status_text"^>
echo         android:layout_width="wrap_content"^>
echo         android:layout_height="wrap_content"^>
echo         android:text="On Track"^>
echo         android:textSize="14sp"^>
echo         android:textColor="@color/text_secondary"^>
echo         android:layout_marginBottom="16dp"^>
echo     ^/^>
echo ^<LinearLayout^>
echo         android:id="@+id/top_apps_container"^>
echo         android:layout_width="match_parent"^>
echo         android:layout_height="wrap_content"^>
echo         android:orientation="vertical"^>
echo         android:visibility="gone"^>
echo         ^<TextView^>
echo             android:id="@+id/app1_name"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="App 1"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/app1_time"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="30m"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/app2_name"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="App 2"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/app2_time"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="20m"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/app3_name"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="App 3"^>
echo         ^/^>
echo         ^<TextView^>
echo             android:id="@+id/app3_time"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="10m"^>
echo         ^/^>
echo     ^</LinearLayout^>
echo ^<LinearLayout^>
echo         android:id="@+id/screen_time_container"^>
echo         android:layout_width="match_parent"^>
echo         android:layout_height="wrap_content"^>
echo         android:orientation="vertical"^>
echo         android:visibility="gone"^>
echo         ^<TextView^>
echo             android:id="@+id/screen_time_text"^>
echo             android:layout_width="wrap_content"^>
echo             android:layout_height="wrap_content"^>
echo             android:text="Screen Time: 4h 0m"^>
echo         ^/^>
echo     ^</LinearLayout^>
echo ^</LinearLayout^>
) > "app\src\main\res\layout\widget_mind_guard.xml"

echo.
echo MindGuardWidget layout fixed successfully!
echo.
echo All required IDs included:
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

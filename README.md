# MindGuard - Digital Wellness App

MindGuard is a native Android app that helps you take control of your screen time and build healthier digital habits. It monitors app usage in real-time, enforces customizable limits, and rewards you with achievements for staying on track.

## Features

### Dashboard
- **Wellness Score** — a daily score (0–100) based on your screen time patterns
- **Time Breakdown** — pie chart showing usage across categories (Deep Work, Entertainment, Communication, Social Media, etc.)
- **App Leaderboard** — see which apps consume the most time
- **Weekly Trends** — bar chart of daily screen time over the past week
- **Timeline** — chronological view of your app usage sessions
- **Streak & Achievements** — track your consistency and milestones

### Focus Sessions
- Start timed focus sessions with a configurable duration (default 25 min)
- Block distracting apps during focus time
- Breathing exercises and motivational quotes
- Celebration with confetti animation on session completion

### Smart App Categorization
- 200+ pre-mapped apps across categories:
  - **Deep Work** — Office, Docs, Notion, Slack, Zoom, learning apps
  - **Communication** — WhatsApp, Telegram, Email
  - **Entertainment** — YouTube, Netflix, TikTok, games
  - **Social Media** — Twitter, Facebook, Reddit, Pinterest
  - **System/Utility** — Settings, Camera, Calculator, File Manager
  - **Neutral** — Browsers, Shopping
- Fully customizable — reassign any app to a different category

### Usage Rules & Interventions
- Set time limits per app, per category, or total screen time
- Trigger types: continuous session or cumulative daily usage
- Actions: notification alerts, overlay warnings, or app blocking
- Three strictness levels: Gentle, Balanced, Strict

### Sleep Guard
- Configurable sleep hours (default 11 PM – 7 AM)
- Monitors and discourages phone usage during sleep time

### Achievement System
- Gamified milestones: first focus session, Instagram-free day, 7-day streak, 1000 minutes saved, Night Owl Slayer
- Categories: Focus, Streak, Productivity, Milestone

### Home Screen Widget
- At-a-glance wellness score and daily stats
- Auto-updates via WorkManager

### Settings
- Notification preferences
- Dark mode support
- App category management
- Usage rules management
- Focus mode configuration
- Data export (CSV)
- Reset to defaults

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| Min SDK | 26 (Android 8.0) |
| Target/Compile SDK | 35 |
| DI | Hilt (Dagger) |
| Database | Room |
| Async | Kotlin Coroutines + Flow |
| Background | WorkManager + Hilt Workers |
| Preferences | Jetpack DataStore |
| UI | ViewBinding, Material Design, ConstraintLayout, ViewPager2, RecyclerView |
| Charts | MPAndroidChart |
| Animations | Konfetti |
| Logging | Timber |
| JSON | Gson |
| Build | Gradle (Kotlin DSL), Version Catalog |

## Project Structure

```
app/src/main/java/com/mindguard/
├── MindGuardApplication.kt          # Application class (Hilt + WorkManager)
├── data/
│   ├── db/                           # Room database, DAOs, type converters
│   ├── model/                        # Data models & Room entities
│   └── repository/                   # Repository layer
├── di/                               # Hilt modules (App, Database, Coroutines, Repository)
├── domain/engine/                    # Business logic (Wellness Score, Rules, Achievements, Focus)
├── intervention/                     # Intervention overlay & quote system
├── service/                          # Background services (Usage Monitor, Blocklist, Boot Receiver)
├── ui/
│   ├── MainActivity.kt
│   ├── dashboard/                    # Dashboard fragment, adapters, view holders
│   ├── achievements/                 # Achievements screen
│   ├── focus/                        # Focus session & completion screens
│   ├── onboarding/                   # Onboarding flow (Welcome, Profile, Permissions, App Review)
│   └── settings/                     # Settings, rules, and app categories screens
├── utils/                            # Permission utilities
├── widget/                           # Home screen widget
└── worker/                           # Background workers (Daily Summary, Behavior Analysis, Sleep Guard, Widget Update)
```

## Getting Started

### Prerequisites

- **Java 17** (JDK)
- **Android SDK** with platform 35 and build-tools 35.0.0
- Set `ANDROID_HOME` environment variable to your SDK location

### Build

```bash
# Clone the repository
git clone https://github.com/yashraj-ghemud/health-app.git
cd health-app

# Build the debug APK
./gradlew assembleDebug

# The APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

### Install

A pre-built debug APK (`MindGuard.apk`) is included in the project root. To install:

1. Transfer `MindGuard.apk` to your Android device
2. Enable **Install from unknown sources** in device settings
3. Open the APK file to install
4. Grant the required permissions when prompted (Usage Access, Notification, Overlay)

## Permissions

| Permission | Purpose |
|-----------|---------|
| `PACKAGE_USAGE_STATS` | Monitor app usage data |
| `SYSTEM_ALERT_WINDOW` | Show intervention overlays |
| `FOREGROUND_SERVICE` | Run usage monitoring in background |
| `RECEIVE_BOOT_COMPLETED` | Restart monitoring after device reboot |
| `POST_NOTIFICATIONS` | Send alerts and daily summaries |
| `READ_CALL_LOG` | Track communication usage |
| `QUERY_ALL_PACKAGES` | Identify installed apps for categorization |

## License

This project is for personal/educational use. 

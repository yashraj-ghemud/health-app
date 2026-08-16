# health-app
> MindGuard — a native Android Kotlin app that tracks foreground app usage, enforces configurable usage rules (notifications/overlays/blocking), provides focus sessions and achievements, and exposes a dashboard and home widget.

## Overview
Compact Android application implemented in Kotlin. The project uses a standard single-module layout with DI (Hilt), Room, DataStore, WorkManager, and modern Android libraries to monitor app usage, run interventions (overlays/notifications), and provide focus/session features.

## What it does
- Tracks foreground app usage and sessions (SessionTracker).
- Maintains an in-memory blocklist with timed unblock behavior (BlocklistManager).
- Shows intervention overlays and notifications (InterventionManager, InterventionOverlayView).
- Loads motivational quotes from assets (QuoteRepository reads app/src/main/assets/quotes.json, with a fallback).
- Persists app state using Room and Jetpack DataStore (DatabaseModule, AppModule).
- Integrates Hilt for DI and WorkManager via a custom Hilt worker factory (MindGuardApplication).

## Key capabilities
- Real-time usage/session tracking.
- In-memory blocklist management with scheduling semantics.
- Intervention overlay UI with motivational quotes and blocking actions.
- Room database and DAOs wired via DI modules.
- DataStore for settings and WorkManager for scheduled/background work.

## Technology
- Kotlin (JVM target 17)
- Android SDK compile/target 35, minSdk 26
- Hilt (Dagger) for DI
- Room (AndroidX) for DB
- Jetpack DataStore for preferences
- WorkManager for background tasks
- Kotlin Coroutines + Flow
- ViewBinding, Material Design, ConstraintLayout, RecyclerView, ViewPager2
- MPAndroidChart, Konfetti, Gson, Timber
- Gradle Kotlin DSL with a version catalog (libs.versions.toml)

## Repository structure
Observed top-level and important paths (abridged from the repository):
- MindGuard.apk
- app/
  - src/main/java/com/mindguard/
    - MindGuardApplication.kt
    - di/ (Hilt modules: App, Database, Coroutines, Repository)
    - data/
      - db/ (Room DB, DAOs)
      - model/
      - repository/
    - domain/engine/
    - intervention/ (overlay + QuoteRepository)
    - service/ (usage monitor, blocklist, BootReceiver)
    - ui/ (activities/fragments: dashboard, focus, onboarding, settings)
    - worker/ (WorkManager workers)
  - src/main/assets/quotes.json
- build.gradle.kts (top-level)
- gradle/, gradlew, settings.gradle.kts

Note: many source excerpts in the supplied dossier are truncated; the above is based on file paths and excerpts provided.

## Getting started
The original project README includes build instructions. Reproduced from the repository evidence:

Prerequisites
- Java 17 (JDK)
- Android SDK with platform 35 and build-tools 35.0.0
- ANDROID_HOME set to your SDK location

Build (from project root)
```bash
git clone https://github.com/yashraj-ghemud/health-app.git
cd health-app

# Build the debug APK
./gradlew assembleDebug

# The APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

Install
- A pre-built APK (MindGuard.apk) is present at the project root per repository contents. The repository README suggests installing that APK on a device and granting required permissions, but reviewers should validate permissions manually (see Configuration / Safety).

## Configuration
What is present in the supplied evidence:
- Top-level build.gradle.kts is present and shows plugin aliases (Kotlin, Android, Hilt, KSP, parcelize) using the version catalog.
- The project uses Gradle Kotlin DSL and a libs.versions.toml (version catalog) for dependency management (evidence in audit).
- DatabaseModule and AppModule are present and provide Room and DataStore wiring.

What is not confirmed from the supplied dossier:
- AndroidManifest.xml content is not present in the provided excerpts; therefore the manifest entries (declared permissions, services, receivers) cannot be confirmed here.
- The quotes asset (app/src/main/assets/quotes.json) is present but was truncated in the supplied excerpt (risk of JSON parsing issues).

Suggested quick inspection steps for contributors
- Check app/src/main/AndroidManifest.xml to confirm required permissions and registration of services/receivers (e.g., BootReceiver, UsageMonitorService). Note: the manifest file was not included in the provided excerpts.
- Inspect app/src/main/assets/quotes.json to verify valid JSON and full quote entries.
- Review DI modules under app/src/main/java/.../di/ (DatabaseModule, AppModule) to see Room/DataStore bindings and other provider bindings.

## Development and quality notes
- Tests: the repository contains only template example tests (ExampleUnitTest and an example androidTest). There are no focused unit/integration tests for core logic classes such as BlocklistManager, SessionTracker, or QuoteRepository in the supplied dossier.
- CI / automation: no CI or workflow files were present in the provided evidence.
- Some source excerpts were truncated in the dossier, so a full code review should begin by opening the complete files under app/src/main/java/.
- The release build in provided configuration had isMinifyEnabled=false (no code shrinking observed in excerpts).

Suggested immediate improvements (based on observed gaps)
- Validate and fix app/src/main/assets/quotes.json so QuoteRepository can parse without falling back.
- Add unit tests for BlocklistManager, SessionTracker, and QuoteRepository to validate concurrency and fallback behavior.
- Confirm manifest entries and runtime permission flows (manifest not supplied in excerpts).

## Safety and responsible use
- Permissions: the README and audit note high-scope permissions referenced by the project (e.g., QUERY_ALL_PACKAGES, READ_CALL_LOG, SYSTEM_ALERT_WINDOW). These require careful justification and correct runtime UX and Play Store policy compliance — verify manifest use and in-app consent flows.
- Overlays: InterventionOverlayView and InterventionManager rely on WindowManager overlays (SYSTEM_ALERT_WINDOW). Overlays can affect user safety and accessibility; minimize surface and validate inputs handled by overlays.
- Data persistence: BlocklistManager appears to keep state in-memory in provided excerpts. If true, block state may be lost on process death/reboot; consider persisting block state if persistence is required.
- Security posture: no evidence in the provided excerpts of secure storage/encryption for potentially sensitive collected data. Review data handling for compliance and user privacy.

## Contributing
No CONTRIBUTING.md or contributing guidelines were present in the supplied evidence. For contributors investigating or making changes:
- Start by opening the app module: app/src/main/java/com/mindguard/
  - DI: di/
  - Database wiring: data/db/ and di/DatabaseModule
  - Core services: service/ (SessionTracker, BlocklistManager, BootReceiver)
  - Intervention UI: intervention/ (InterventionManager, InterventionOverlayView, QuoteRepository)
  - Assets: app/src/main/assets/quotes.json
- Validate AndroidManifest.xml (app/src/main/AndroidManifest.xml) to confirm permissions and registrations; the manifest was not included in the dossier excerpts and must be checked directly in the repository.
- Run the build locally using the provided Gradle commands to reproduce the APK build and to surface any compile-time issues caused by truncated or missing files.

## License
No license information was provided in the supplied evidence.

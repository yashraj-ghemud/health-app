@echo off
echo Testing BehaviorAnalysisWorker type annotation fixes...
echo.

REM Build the project to verify no compilation errors
gradlew.bat assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo SUCCESS: BehaviorAnalysisWorker type annotation fixes completed!
    echo.
    echo Fixed lambda expressions with explicit type annotations:
    echo.
    echo 1. identifyRiskApps function:
    echo    - filter { (it as UsageEvent): UsageEvent -> it.category == PASSIVE_ENTERTAINMENT }
    echo    - groupBy { (it as UsageEvent): UsageEvent -> it.packageName }
    echo    - map { (packageName: String, events: List<UsageEvent>) -> ... }
    echo    - sumOf { (event: UsageEvent): UsageEvent -> event.durationMinutes }
    echo.
    echo 2. analyzeWeekdayWeekendPatterns function:
    echo    - forEach { (summary: DailySummary): DailySummary -> ... }
    echo.
    echo 3. generateRuleSuggestions function:
    echo    - forEach { (pattern: BehaviorPattern): BehaviorPattern -> ... }
    echo    - forEach { (riskApp: RiskApp): RiskApp -> ... }
    echo    - forEach { (rule: UsageRule): UsageRule -> ... }
    echo.
    echo 4. analyzeEntertainmentTrend function:
    echo    - map { (summary: DailySummary): DailySummary -> summary.entertainmentMinutes }
    echo.
    echo 5. analyzeLateNightUsage function:
    echo    - count { (summary: DailySummary): DailySummary -> summary.wellnessScore < 70 }
    echo.
    echo 6. analyzeProductivityPatterns function:
    echo    - map { (summary: DailySummary): DailySummary -> summary.productiveMinutes }
    echo.
    echo All lambda expressions now have explicit type annotations for Kotlin compiler resolution.
) else (
    echo.
    echo BUILD FAILED: There may still be type annotation issues.
)

echo.
pause

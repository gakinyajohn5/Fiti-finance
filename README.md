# Fiti-Finance — Comrade App

A native Android app (Kotlin + Jetpack Compose + Room) implementing the full Fiti-Finance blueprint:

1. **Comrade Persona Onboarding Wizard** — 4-step profile builder (living situation, roommates, meal style, party profiling) that drives dynamic dashboard visibility.
2. **Time-Aware Visual Meal Planner** — `java.time.LocalTime`-based meal windows (Breakfast/Lunch/Supper/Late Night), real-time budget deductions, and a natural-language command bar ("minus 100 KES", "swap supper to Rice Beans").
3. **M-PESA SMS Parser** — a `BroadcastReceiver` that parses Paybill/Till/Pochi la Biashara messages automatically and pops a categorization bottom sheet for Peer-to-Peer transfers, with "always remember this rule" support.
4. **Night-Out & Party Budgeting** — full drink database, Full-Payment vs Split-Bill calculator with Pending Receivables tracking, and an Emergency Fare Shield.
5. **Location-Aware Context Engine** — Campus (Navy/Slate), Bar (Neon Dark/AMOLED), and Kibanda (Warm Amber/Green) themes, switchable live from the dashboard (wired to `FusedLocationProviderClient` for real geofence-style detection; manual override included for demo/emulator use since real POI geofencing needs a Google Places/Maps API key you supply).
6. **Savings Jars & AI Advice Engine** — goal jars with priority-based auto-allocation, an on-device heuristic advice engine by default, and an optional `GeminiAdviceEngine` you can wire in with your own API key for richer natural-language suggestions.

## Project structure

```
app/src/main/java/com/fitifinance/comrade/
  data/            Room entities, DAOs, database, type converters
  repository/      FinanceRepository — single source of truth for the UI
  engine/          Pure Kotlin logic: meal timing, food/drink DBs, bill split,
                   savings allocation, AI advice, location/theme engine, alarms
  sms/             SMS BroadcastReceiver + M-PESA regex parser + categorizer
  viewmodel/       One StateFlow-based ViewModel per screen
  ui/               Jetpack Compose screens (onboarding, dashboard, meals,
                   night-out, savings, transactions) + Material3 theme
  navigation/      Compose Navigation graph
```

## Building via GitHub

This repo includes a Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle/wrapper/`) and a
GitHub Actions workflow at `.github/workflows/android-build.yml` that builds a debug APK
on every push to `main` and uploads it as a workflow artifact.

**To use it:**
1. Push this project to a new GitHub repository.
2. Go to the **Actions** tab — the "Android Build" workflow runs automatically.
3. Once it finishes, open the run and download the `fiti-finance-debug-apk` artifact.

You can also open the project directly in **Android Studio** (Koala/2024.1+) and run it
on a device/emulator — Android Studio will regenerate/verify the Gradle wrapper itself.

## Permissions

The app requests these at first launch (all are used, none are decorative):
- `RECEIVE_SMS` / `READ_SMS` — for the M-PESA transaction parser
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — for the location-aware theme engine
- `POST_NOTIFICATIONS` — for the morning-after "Damage Control" summary
- `INTERNET` — only used if you enable the optional Gemini-backed advice engine

If you don't grant SMS/location permissions, those specific modules simply stay dormant
(manual meal/drink logging and the theme switcher on the dashboard still work fully).

## Wiring up the optional Gemini AI advice engine

By default `FitiApplication` uses `LocalHeuristicAdviceEngine` (fully on-device, no
network, no key needed). To switch to Gemini-backed natural-language advice:

```kotlin
// In FitiApplication.onCreate(), after repository is built:
adviceEngine = GeminiAdviceEngine(apiKey = BuildConfig.GEMINI_API_KEY)
```

Add your key to `local.properties` (never commit it) and expose it via `BuildConfig`
in `app/build.gradle.kts`, e.g.:

```kotlin
buildConfigField("String", "GEMINI_API_KEY", "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\"")
```//
and set `buildFeatures { buildConfig = true }`.

## What's implemented vs. stubbed

Fully implemented: onboarding logic, Room persistence, SMS regex parser + categorizer,
time-aware meal engine + NL command parsing, bill-split calculator + fare shield,
savings auto-allocation + conversational top-ups, on-device advice engine, morning-after
notification via `AlarmManager`-triggered `BroadcastReceiver`, and every screen's UI.

Intentionally left as integration points (they need your own API keys/config, not code):
- Real Google Places/OSM POI geofencing (the proximity-matching engine is implemented;
  you supply the POI list + Places API key for production use — a manual mode switch is
  included on the dashboard for demoing without it).
- `GeminiAdviceEngine` cloud calls (implemented, just needs your API key wired in as above).

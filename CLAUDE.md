# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Left** is an Android app that visualizes time passing through dot grids, circular rings, and barcodes. It tracks year, month, week, day, hour, and lifetime progress using purely visual representations (no text labels on visualizations). Inspired by Tim Urban's "Your Life in Weeks."

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (minified with ProGuard)
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumentation tests on device/emulator
```

Requires JDK 17. Min SDK 26, target SDK 34. Kotlin 1.9.24 with Compose compiler 1.5.14.

## Architecture

Single-activity Compose app (`MainActivity`) with one main screen (`LeftScreen`). No ViewModel layer — state flows directly from DataStore preferences through the activity to composables.

### Data Flow

```
DataStore (UserPreferences) ──→ MainActivity ──→ LeftScreen ──→ Visualization Components
Room DB (CustomDateEntity)  ──→ TimeRepository ──↗
```

- **UserPreferences** (DataStore): All settings — birth date, theme, colors, symbol type, active hours, notifications. Exposes reactive `Flow<UserPreferencesData>`.
- **Room database** (`AppDatabase`): Stores custom countdown events via `CustomDateDao`.
- **TimeRepository**: Bridges domain models to Room persistence for countdown dates.

### UI Layer

- **LeftScreen**: Main screen with horizontal swipe navigation between 6 time units (LIFE, YEAR, MONTH, WEEK, DAY, HOUR). Uses `AnimatedContent` for transitions.
- **Visualization components** each render via Compose `Canvas`:
  - `DotGrid` — adaptive column layout using `sqrt(N * W/H)` optimization
  - `MonthCalendar` — 7-column grid with day-of-week offset
  - `WeekView` — 7 large dots in a row
  - `DayGrid` — circular ring of hour dots
  - `HourClock` — 60 dots in clock-face circle
- **TimeSelector**: Horizontal pill bar for switching time units.
- **SettingsSheet**: Modal bottom sheet for all preferences.

### Theme System

4 theme packs (`ThemePack.kt`): NOIR, SOLAR, OCEANIC, PAPER — each with dark/light variants. Each pack defines background, surface, text, accent, ambient gradient colors, and pre-defined elapsed/remaining color palettes. Theme selection stored in DataStore as a string enum.

### Widget System

6 Glance-based home screen widgets, all rendered via off-screen `Bitmap` + `Canvas` in `WidgetRenderer`:
- Rendering pipeline: `WidgetRenderer` draws bitmaps → Glance composable displays bitmap + metadata → click handler launches `MainActivity` with `"time_unit"` intent extra
- `WidgetUpdateWorker` (WorkManager) refreshes all widgets every 30 minutes
- `WidgetVisualStyle` maps theme packs to widget-appropriate colors
- Widget types: YearProgress, YearBarcode, MonthProgress, LifeProgress, Countdown (ring), DayHour

### Key Utilities

- **TimeCalculations.kt**: Pure functions for all date/time math (leap-year aware, configurable active hours, life expectancy lookup table by country/gender)
- **ShareHelper.kt**: Generates bitmap of current visualization for sharing via `FileProvider`
- **NotificationHelper.kt**: Daily summaries and milestone detection (50%, 75%, 100%)

## Conventions

- Colors stored as hex strings in DataStore, parsed to `Color`/`Int` at usage site
- Time library: `java.time.*` (LocalDate, LocalDateTime) — no third-party date libs
- Domain models use enums: `TimeUnit` (6 values), `SymbolType` (7 values)
- All widget receivers declared in AndroidManifest with corresponding `*_widget_info.xml` metadata
- Single module Gradle project (`:app`)

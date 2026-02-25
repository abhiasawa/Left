# Left (TimeLeft)

Left is a visual-first Android app for people who want to *feel* time passing, not just read numbers.

Track your life, year, month, week, day, and hour with clean dot-based visuals and home screen widgets.

Inspired by Tim Urban's "Your Life in Weeks" and [Left - Widgets for Time Left](https://apps.apple.com/app/left-days-of-the-year/id1533146565).

## Why Left

- Visual over numeric: no clutter, no charts, no spreadsheets
- Fast daily check-in: open app or glance at widgets
- Personal and customizable: symbols, colors, active hours, countdowns
- Built for homescreen habits: widgets that update and deep-link into the app

## App Preview

| Year | Life |
|---|---|
| ![Year view](screenshots/app-year.png) | ![Life view](screenshots/app-life.png) |

| Month | Week |
|---|---|
| ![Month view](screenshots/app-month.png) | ![Week view](screenshots/app-week.png) |

## Widget Preview

| Year Progress (2x2) | Month Progress (2x2) |
|---|---|
| ![Year widget](screenshots/widget-year-progress.png) | ![Month widget](screenshots/widget-month-progress.png) |

| Life Progress (2x2) | Countdown (2x2) |
|---|---|
| ![Life widget](screenshots/widget-life-progress.png) | ![Countdown widget](screenshots/widget-countdown.png) |

| Day / Hour (2x2) | Year Barcode (4x2) |
|---|---|
| ![Day hour widget](screenshots/widget-day-hour.png) | ![Year barcode widget](screenshots/widget-year-barcode.png) |

Also included as share-ready collages:

- ![Widgets overview](screenshots/widgets-overview.png)
- ![Widgets full set](screenshots/widgets-full-set.png)

## Install Directly On Android (No Build Needed)

Tap this link from your Android phone:

- [Download TimeLeft APK (v1.0.0)](https://github.com/abhiasawa/Left/raw/main/downloads/TimeLeft-v1.0.0-debug.apk)

Install steps:

1. Download the APK.
2. Open it from the Downloads app.
3. If prompted, allow installs from your browser/files app.
4. Tap Install.

## Build From Source (Optional)

```bash
./gradlew :app:installDebug
```

Or build the APK only:

```bash
./gradlew :app:assembleDebug
```

## Core Product Features

- Life/year/month/week/day/hour visual timelines
- Real-time "current moment" marker
- Swipe between time units
- 7 symbol styles (dot, star, heart, hexagon, square, diamond, number)
- Custom color themes for elapsed/remaining/current indicators
- Share snapshots
- Daily reminders + milestone detection
- Configurable wake/sleep window for day/hour views

## Tech Stack

- Kotlin + Jetpack Compose + Material 3
- Glance AppWidget
- Room + DataStore
- WorkManager
- Min SDK 26 / Target SDK 34

## Project Structure

```text
app/src/main/java/com/timeleft/
├── data/
├── domain/
├── navigation/
├── ui/
├── util/
├── widgets/
├── MainActivity.kt
└── TimeLeftApplication.kt
```

## License

MIT

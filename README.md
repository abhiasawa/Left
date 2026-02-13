# Left - Time Left Visualizer

An Android app that visualizes time passing through purely visual dot grids, circular rings, and barcodes. Track how much of the year, month, week, day, hour, or your life has elapsed — and how much remains. No numbers, no labels — the visualization IS the interface.

Inspired by Tim Urban's "Your Life in Weeks" and the iOS app [Left - Widgets for Time Left](https://apps.apple.com/app/left-days-of-the-year/id1533146565).

## Features

### Visualizations

Each time scale has a purpose-built, purely visual representation:

- **Life** — Tim Urban-style grid: 52 columns (weeks) × rows (years). One dot = one week of your life.
- **Year** — Adaptive dot grid filling the screen. One dot per day of the year.
- **Month** — 7-column dot grid matching calendar layout with day-of-week offset. Reads as a familiar calendar shape without any text.
- **Week** — 7 large dots in a horizontal row. Universally recognizable as a week.
- **Day** — Circular ring of dots (one per active hour). Every 6th dot is a larger quadrant marker.
- **Hour** — 60 dots arranged in a clock-face circle. One dot per minute.

Elapsed time is dimmed, the current moment pulses red, remaining time is bright. Swipe horizontally to switch between scales.

### Home Screen Widgets

6 Glance-based widgets with dark backgrounds and click-to-open:

| Widget | Size | Description |
|--------|------|-------------|
| Year Progress | 2×2 | Dot grid of days in the year |
| Year Barcode | 4×2 | Barcode-style year visualization |
| Month Progress | 2×2 | Dot grid of days in the month |
| Life Progress | 2×2 | Dot grid of weeks in your life |
| Countdown | 2×2 | Ring progress for next countdown |
| Day/Hour | 2×2 | Dot grid of hours in the day |

Tapping any widget opens the app directly to the relevant time scale.

### Other

- 7 symbol types (dot, star, heart, hexagon, square, diamond, number)
- Custom color theming for elapsed, remaining, and current indicator
- Share progress as images
- Daily summary notifications with milestone detection
- Configurable active hours (wake/sleep)

## Tech Stack

- **UI**: Jetpack Compose + Material 3 + Canvas drawing
- **Widgets**: Glance AppWidget framework
- **Preferences**: DataStore
- **Background**: WorkManager (widget updates, notifications)
- **Min SDK**: 26 (Android 8.0)
- **Language**: Kotlin

## Project Structure

```
app/src/main/java/com/timeleft/
├── data/
│   ├── preferences/         # DataStore user preferences
│   └── repository/          # Data repository
├── domain/
│   └── models/              # TimeUnit, SymbolType
├── navigation/              # NavHost routing
├── ui/
│   ├── components/          # DotGrid, DayGrid, WeekView, MonthCalendar,
│   │                        # HourClock, TimeSelector
│   ├── screens/             # LeftScreen (main visualization)
│   ├── settings/            # Settings bottom sheet
│   └── theme/               # Colors, typography, theme
├── util/                    # TimeCalculations, ShareHelper
├── widgets/                 # 6 Glance widgets + WidgetRenderer
├── MainActivity.kt
└── TimeLeftApplication.kt
```

## Building

1. Open the project in Android Studio (Hedgehog or newer)
2. Sync Gradle
3. Run on a device/emulator with API 26+

## License

MIT

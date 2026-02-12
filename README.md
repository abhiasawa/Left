# Left - Time Left Widgets

An Android app that visualizes time passing through dot grids, barcodes, and progress rings. Track how much of the year, month, week, day, or your life has elapsed — and how much remains.

Inspired by the iOS app [Left - Widgets for Time Left](https://apps.apple.com/app/left-days-of-the-year/id1533146565).

## Features

### Screens

- **Left** — Dot grid visualization for year, month, week, day, and hour progress. Supports 7 symbol types (dot, star, heart, hexagon, square, diamond, number) and custom colors.
- **Ahead** — Custom countdown dates with color-coded progress bars. Add events like birthdays, deadlines, or vacations.
- **You** — Life expectancy visualization based on birth date, gender, and country. Uses actuarial data for 20 countries.

### Home Screen Widgets

6 Glance-based widgets:

| Widget | Size | Description |
|--------|------|-------------|
| Year Progress | 2×2 | Dot grid of days in the year |
| Year Barcode | 4×2 | Barcode-style year visualization |
| Month Progress | 2×2 | Dot grid of days in the month |
| Life Progress | 2×2 | Dot grid of years in your life |
| Countdown | 2×2 | Ring progress for next countdown |
| Day/Hour | 2×2 | Dot grid of hours in the day |

### Other

- Share progress as images
- Daily summary notifications with milestone detection
- Dark/light theme support
- Symbol and color customization

## Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Widgets**: Glance AppWidget framework
- **Database**: Room (custom countdown dates)
- **Preferences**: DataStore
- **Background**: WorkManager (widget updates, notifications)
- **Min SDK**: 26 (Android 8.0)
- **Language**: Kotlin

## Project Structure

```
app/src/main/java/com/timeleft/
├── data/
│   ├── db/                  # Room database, DAO, entities
│   ├── preferences/         # DataStore user preferences
│   └── repository/          # Data repository
├── domain/
│   ├── models/              # TimeUnit, SymbolType, CustomDate
│   └── usecases/            # Business logic
├── navigation/              # Bottom nav with NavHost
├── ui/
│   ├── components/          # DotGrid, TimeSelector, SymbolPicker
│   ├── screens/             # Left, Ahead, You screens
│   ├── settings/            # Settings bottom sheet
│   └── theme/               # Colors, typography, theme
├── util/                    # TimeCalculations, ShareHelper, NotificationHelper
├── widgets/                 # 6 Glance widgets + renderer + updater
├── MainActivity.kt
└── TimeLeftApplication.kt
```

## Building

1. Open the project in Android Studio (Hedgehog or newer)
2. Sync Gradle
3. Run on a device/emulator with API 26+

## License

MIT

# Weather

A native Android weather app (Java) built with Retrofit, Room, Glide, and MPAndroidChart. It lets a user log in, search or auto-detect a city, view a 5‑day forecast, save favorite cities, and drill into hourly detail (temperature chart, sunrise/sunset arc, humidity, wind, pressure, visibility).

## Features

- **Onboarding flow**: Welcome screen → simple username login (stored in `SharedPreferences`) → location screen with a personalized greeting.
- **Location input**: Search by city name, or fetch current GPS coordinates via `FusedLocationProviderClient` (with runtime permission handling for `ACCESS_FINE_LOCATION`).
- **Current conditions + 5‑day forecast**: Pulled from the OpenWeatherMap `forecast` endpoint, showing temperature, "feels like", high/low, condition text, and a matching weather icon.
- **Detail screen**: Hourly temperature line chart (next ~24h, via MPAndroidChart), humidity, pressure, wind speed, visibility, and a custom sunrise/sunset arc view (`SunArcView`).
- **Saved cities list**: Add/search cities, persisted per-user in a local Room database (`weather_db`), with duplicate checking and live temperature refresh for each saved city.
- **Offline-friendly UX touches**: Network availability check before API calls, error toasts on failures.

## Screens / Activities

| Activity | Purpose |
|---|---|
| `MainActivity` | Landing/welcome screen, entry point (`LAUNCHER`) |
| `LoginActivity` | Captures a username, saves it to `SharedPreferences` |
| `LocationActivity` | Greets the user, lets them search a city or use current GPS location |
| `InfoActivity` | Shows current weather + 5‑day forecast for the chosen city/coords |
| `DetailActivity` | Hourly chart, sunrise/sunset arc, humidity/pressure/wind/visibility |
| `search_city` | Manage a saved list of cities (add, view, tap to open `InfoActivity`) |

## Tech Stack

- **Language**: Java (Android, minSdk 24 / targetSdk & compileSdk 35)
- **Networking**: Retrofit2 + Gson converter, calling the OpenWeatherMap `/data/2.5/forecast` API
- **Local storage**: Room (single `cities` table via `CityEntity` / `CityDao` / `AppDatabase`)
- **Images**: Glide (remote images) + bundled custom weather icons (sunny, rainy, snowy, storm, cloudy, partly cloudy)
- **Charts**: MPAndroidChart (`LineChart` for hourly temperature trend)
- **Location**: Google Play Services `FusedLocationProviderClient`
- **UI**: AndroidX (AppCompat, Material Components, ConstraintLayout, RecyclerView)
- **Build**: Gradle Kotlin DSL (`build.gradle.kts`), version catalog (`libs.versions.toml`)

## Project Structure

```
app/src/main/java/com/example/weather/
├── MainActivity.java          # Welcome screen
├── LoginActivity.java         # Username capture
├── LocationActivity.java      # City search / GPS location entry
├── InfoActivity.java          # Current weather + 5-day forecast
├── DetailActivity.java        # Hourly chart + detailed stats
├── search_city.java           # Saved cities list (Room-backed)
├── AppDatabase.java           # Room database
├── CityDao.java                # Room DAO for saved cities
├── adapter/
│   ├── CityAdapter.java       # Saved-cities RecyclerView adapter
│   ├── DailyAdapter.java      # 5-day forecast RecyclerView adapter
│   └── HourlyAdapter.java     # Hourly forecast RecyclerView adapter
├── api/
│   ├── WeatherApi.java        # Retrofit endpoint definitions
│   └── RetrofitClient.java    # Retrofit singleton (OpenWeatherMap base URL)
└── model/
    ├── WeatherResponse.java, WeatherItem.java, Main.java, Weather.java, Wind.java
    ├── Cities.java, City.java, CityEntity.java   # UI + Room models
    └── SunArcView.java         # Custom sunrise/sunset arc View
```

## Setup

1. **Clone/open** the project in Android Studio (Meerkat/Koala or newer recommended for `compileSdk 35`).
2. **OpenWeatherMap API key**: The app currently has an API key hardcoded directly in `InfoActivity.java`, `DetailActivity.java`, and `search_city.java`. For any real use, replace these with your own key from [openweathermap.org](https://openweathermap.org/api) and, ideally, move it out of source control (e.g. into `local.properties` / `BuildConfig`, or a `gradle.properties` value injected via `buildConfigField`) rather than hardcoding it in three places.
3. **Permissions**: The app requests `INTERNET`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, and `ACCESS_NETWORK_STATE`. Location permission is requested at runtime when the user taps "use current location."
4. **Build & run**: Use the Gradle wrapper — `./gradlew assembleDebug` (or run directly from Android Studio on an emulator/device, min Android 7.0 / API 24).

## Known Gaps / Notes

- API key is hardcoded (see Setup above) rather than injected via secure config — fine for a learning/demo project, not for publishing.
- `txtUV` (UV index) on the detail screen is currently hardcoded to `"Low"` rather than pulled from a real data source — OpenWeatherMap's free `forecast` endpoint doesn't include UV index, so this would need the separate One Call/UV Index API.
- Login is local-only (no backend/auth) — it's used purely to namespace saved cities per device user via `SharedPreferences` + the `username` column in Room.
- No unit/instrumentation test coverage beyond the default generated `ExampleUnitTest` / `ExampleInstrumentedTest` stubs.

# HMX Calculator

A native Kotlin + Jetpack Compose / Material 3 calculator (minimum Android 6.0 / API 23).

## Features

- Arithmetic, percentages, decimals, unary signs and parentheses, parsed without `eval()`.
- Live preview, floating-point result formatting, division-by-zero and invalid-expression handling.
- Calculation history with tap-to-reuse and clear; copy result to clipboard.
- Persistent dark/light theme and haptic preference.
- Saveable expression/result state and adaptive Compose layout.

## Project layout

- `app/src/main/java/com/hmx/calculator/CalculatorEngine.kt` — isolated safe expression parser and formatter.
- `app/src/main/java/com/hmx/calculator/MainActivity.kt` — Compose UI and preference-backed app state.
- `app/src/test/java/com/hmx/calculator/CalculatorEngineTest.kt` — engine unit tests.

## Build and test (Android Studio)

Open this directory as a Gradle project in Android Studio. Use JDK 17+, Android SDK 35, then run `gradle testDebugUnitTest` and `gradle assembleDebug` (or generate a Gradle wrapper in Android Studio). The generated debug APK is under `app/build/outputs/apk/debug/`.

## Cloud build with GitHub Actions

The workflow at `.github/workflows/android.yml` runs the unit tests and builds a debug APK on GitHub-hosted Ubuntu for pushes to `main`/`master`, pull requests, or manual dispatch. Download the `hmx-calculator-debug-apk` artifact from the completed workflow run (retained for 14 days).

A local build was attempted on the phone with a 512 MB Gradle heap and one worker, but stopped before completion due to limited available memory. No local debug APK was produced; cloud build and test results are not yet available.

# Android Demo

Minimal native Android Java app built entirely in GitHub Actions, without a locally installed Android SDK or Android Studio.

## Build APK

1. Open **Actions** → **Build Android APK**.
2. Select **Run workflow** on the `main` branch.
3. Download the `android-demo-debug-apk` artifact after the run succeeds and extract the APK.

The app presents a Hebrew RTL screen with a tap counter and a reset button. The APK is debug-signed for testing, not Play Store publication.

## Development

- JDK 17
- Gradle 8.10.2
- Android Gradle Plugin 8.7.3
- compileSdk 35, minSdk 23

Android build dependencies are installed only on the GitHub-hosted runner.

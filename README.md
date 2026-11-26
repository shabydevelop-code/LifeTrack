# Android Demo — מעקב אכילה אישי

Native Kotlin app using Jetpack Compose, Material 3 and a ViewModel. Safe drawing insets and RTL are supported.

Build via Actions > Build Android APK > Run workflow on main. Download Android-Demo.apk from the latest release. The permanent testing keystore must be configured in the ANDROID_KEYSTORE_BASE64 repository secret. Never commit the keystore.

JDK 17, Gradle 8.10.2, AGP 8.7.3, Kotlin 2.0.21, compileSdk 35, minSdk 23. No local Android SDK is required. Room persistence is not yet implemented.

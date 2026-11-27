# LifeTrack

Local lifestyle tracker for Android, built with Kotlin Multiplatform and Compose Multiplatform. Android is the active target; adding iOS is deferred until explicitly approved.

## Architecture

| Module / source set | Responsibility |
| --- | --- |
| `shared/src/commonMain` | Domain models, repository contracts, report calculations, CounterViewModel, shared screens, design system, navigation shell and feedback |
| `shared/src/androidMain` | Android implementations of shared platform services, including clock and report calendar |
| `app/src/main` | Android entry point, native navigation, SQLite repositories, biometric authentication, lifecycle, native pickers, image resources and platform adapters |
| `shared/src/androidUnitTest` | Shared logic JVM tests |
| `app/src/test` | JUnit and Robolectric repository, SQLite and precision regression tests |

Platform services are injected into shared code through interfaces and callbacks. Weight screen state is currently managed within the shared composable; exact decimal conversion is supplied by the Android adapter. Data remains local in `eattrack.db`; weights are stored as integer grams. Preserve existing data through explicit schema migrations.

## Build and tests

GitHub Actions runs `gradle --no-daemon :shared:testDebugUnitTest :app:testDebugUnitTest` before assembling the APK. Failed tests block publication. Device UI, accessibility and biometric behavior require separate device verification.

Build only after explicit approval, using workflow_dispatch or an approved update to `.github/build-request.txt`. The APK is published as `LifeTrack.apk`. Permanent signing uses the existing `ANDROID_KEYSTORE_BASE64` repository secret.

Application ID: `com.shabydevelop.eattrack`. Preserve this ID and signing certificate for in-place updates.

Development rules: [AGENTS.md](AGENTS.md).

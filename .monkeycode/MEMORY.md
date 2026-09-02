# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Entries

[Project Knowledge Summary]
- Date: 2026-08-30
- Context: Discovered by Agent while preparing the Android release build
- Category: Build Methods
- Instructions:
  - The project uses Gradle Wrapper 8.7, Android Gradle Plugin 8.6.1, Kotlin 2.0.21, Compose compiler plugin, KSP 2.0.21-1.0.28, and Room 2.6.1.
  - The current workspace has no Java runtime or Android SDK, so Gradle builds stop before dependency resolution.

[Project Knowledge Summary]
- Date: 2026-09-02
- Context: Discovered by Agent while assembling the Debug APK
- Category: Build Methods
- Instructions:
  - Debug APK generation in this environment uses JDK 17 at `/usr/lib/jvm/java-17-openjdk-amd64` and Android SDK at `/opt/android-sdk`.
  - Required SDK packages are Platform 35 and Build Tools 35.0.1.
  - Debug APK output path is `app/build/outputs/apk/debug/app-debug.apk`.

[Project Knowledge Summary]
- Date: 2026-08-30
- Context: Discovered by Agent while performing final Android static verification
- Category: Environment Configuration
- Instructions:
  - The workspace currently has no global `gradle`, `gradlew`, `kotlinc`, or `adb` executable available.
  - Android compilation and device verification must run in Android Studio or another environment with the configured Gradle toolchain and Android SDK.

[Project Knowledge Summary]
- Date: 2026-08-30
- Context: Discovered by Agent while initializing the Android project
- Category: Build Methods
- Instructions:
  - Android project build configuration targets compileSdk 35, targetSdk 35, minSdk 29, JDK 17, and Gradle 8.7.
  - The application module is built with Gradle Kotlin DSL and uses the Android Gradle Plugin.

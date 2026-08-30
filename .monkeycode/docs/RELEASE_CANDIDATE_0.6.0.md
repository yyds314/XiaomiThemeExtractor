# Global Font Manager 0.6.0 Release Candidate

Date: 2026-08-30
Status: Blocked pending external Android build environment

## Version

- versionName: `0.6.0`
- versionCode: `6`
- Gradle Wrapper: `8.7`
- Android Gradle Plugin: `8.6.1`
- Kotlin: `2.0.21`
- KSP: `2.0.21-1.0.28`
- compileSdk: `35`
- targetSdk: `35`
- minSdk: `29`

## Environment Verification

| Check | Result |
| --- | --- |
| `java -version` | Blocked: `java` not found |
| `./gradlew --version` | Blocked: `java` not found |
| `sdkmanager --list` | Blocked: `sdkmanager` not found |
| Android Studio command/install | Not found in workspace environment |
| Android SDK 35 | Not installed in workspace |
| Build Tools 35.0.1 | Not installed in workspace |
| Gradle Wrapper JAR | Present |
| `gradlew` executable bit | Present |

## Build Verification

The requested commands were executed. Each Gradle command stopped before Gradle initialization with exit code 127 because Java is unavailable.

```text
./gradlew clean                         exit 127
./gradlew assembleDebug                 exit 127
./gradlew lint                          exit 127
./gradlew test                          exit 127
./gradlew assembleRelease               exit 127
```

## APK Output

- Debug APK: not generated
- Expected path: `app/build/outputs/apk/debug/app-debug.apk`
- Release output directory: not generated
- APK size: unavailable
- SHA256: unavailable

## Static Release Checks

- `AndroidManifest.xml` contains FileProvider configuration and Root manager package visibility entries.
- `res/xml/file_paths.xml` exposes module exports and diagnostics within app-private files.
- Module generation includes `module.prop`, `post-fs-data.sh`, `service.sh`, `customize.sh`, and `uninstall.sh`.
- Module shell scripts are assigned executable permissions and ZIP validation requires mode `755`.
- Font payload files are validated with mode `644`.
- Root operations use the fixed module path and quoted shell arguments.
- Release signing uses the Android debug signing configuration for test builds. A production keystore is required for distribution.

## External Validation Steps

1. Install JDK 17 and configure `JAVA_HOME`.
2. Install Android SDK Platform 35 and Build Tools 35.0.1.
3. Open the workspace root in Android Studio and complete Gradle Sync.
4. Run `./gradlew clean`.
5. Run `./gradlew assembleDebug`, `./gradlew lint`, and `./gradlew test`.
6. Run `./gradlew assembleRelease`.
7. Record APK size and SHA256 from the generated output.
8. Install the Debug APK on Android 10-16 test devices.
9. Validate Root provider detection, module installation, rollback, TTC fonts, SELinux warnings, and diagnostic export.

## Release Risks

- No APK has been produced in the current workspace.
- Kotlin, Compose, KSP, Room, and Manifest compilation remain unverified until the external toolchain is available.
- Release artifacts are test-signed with the debug keystore.
- Magisk, KernelSU, APatch, MIUI, and HyperOS behavior requires real-device validation.

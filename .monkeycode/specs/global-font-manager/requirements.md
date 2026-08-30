# Requirements Document

## Introduction

Global Font Manager is a Root-enabled Android font management application for Xiaomi, Redmi, and POCO devices running MIUI or HyperOS. The second phase implements private font import, OpenType metadata parsing, Room persistence, search, sorting, and preview.

## Glossary

- **Font Library**: The application area that lists managed font files.
- **Root Service**: The service boundary reserved for privileged device operations.
- **Theme**: The light or dark Material 3 appearance of the application.

## Requirements

### Requirement 1: Project Foundation

**User Story:** As a developer, I want a standard Kotlin Android project, so that the application can be opened and extended in Android Studio.

#### Acceptance Criteria

1. The project SHALL use Kotlin, Gradle Kotlin DSL, Android Gradle Plugin, and Java 17 compatibility.
2. The application SHALL declare compileSdk 35, targetSdk 35, and minSdk 29.
3. The project SHALL contain separate data, domain, repository, UI, service, and utils layers.

### Requirement 2: Application Shell

**User Story:** As a user, I want a clear application shell, so that I can access the main areas of the font manager.

#### Acceptance Criteria

1. WHEN the application starts, the system SHALL display the home screen.
2. WHEN a user selects a bottom navigation destination, the application SHALL display the selected destination.
3. The application SHALL provide home, font library, and settings destinations.

### Requirement 3: Theme Management

**User Story:** As a user, I want light and dark themes, so that the interface matches my preference.

#### Acceptance Criteria

1. The application SHALL render all screens with Material Design 3 components and color schemes.
2. WHEN a user changes the dark mode switch, the application SHALL update the application theme.
3. WHEN no explicit theme choice is made, the application SHALL use the system dark mode preference.

### Requirement 4: Font Import

**User Story:** As a user, I want to import font files through the system picker, so that the application can manage fonts within Scoped Storage.

#### Acceptance Criteria

1. WHEN a user selects TTF, OTF, or TTC files with SAF, the application SHALL copy each valid file to the private fonts directory.
2. WHEN a copied file is readable, the application SHALL create a database record for the file.
3. IF a selected file has an unsupported extension or invalid font structure, the application SHALL show an import error and preserve the existing library records.

### Requirement 5: Font Metadata and Preview

**User Story:** As a user, I want to inspect and preview real font metadata, so that I can choose the correct font before a future system application step.

#### Acceptance Criteria

1. The application SHALL parse names, family, author, version, type, size, import time, and Unicode range information from TTF, OTF, and TTC files.
2. WHEN a user opens a font card, the application SHALL display the stored metadata and sample Chinese, English, and numeric text.
3. The application SHALL load the imported file as a runtime Typeface for the preview.

### Requirement 6: Library Management

**User Story:** As a user, I want to search and sort my font library, so that I can find a font efficiently.

#### Acceptance Criteria

1. The font library SHALL display a preview, name, type, import time, and application status for each record.
2. WHEN a user enters a search term, the application SHALL filter by font name or family name.
3. WHEN a user selects a sort mode, the application SHALL sort records by name, time, or type.

### Requirement 7: Root Overlay Engine

**User Story:** As a rooted-device user, I want system fonts applied through a module overlay, so that the read-only system partitions remain unchanged.

#### Acceptance Criteria

1. WHEN the application checks the device, the application SHALL report `su` availability, command permission, Root provider, and module path.
2. WHEN a user applies a font, the application SHALL detect existing supported font targets under `/system/fonts`, `/product/fonts`, and `/system_ext/fonts`.
3. The application SHALL generate and install a Magisk-compatible overlay module with `module.prop`, `system/fonts`, `post-fs-data.sh`, `service.sh`, and `uninstall.sh`.
4. The application SHALL map detected product and system extension paths into the module overlay hierarchy.

### Requirement 8: Backup and Recovery

**User Story:** As a rooted-device user, I want automatic backups and recovery, so that a failed module operation can be reversed.

#### Acceptance Criteria

1. WHEN a font application begins, the application SHALL back up every detected target before module installation.
2. The application SHALL persist original path, backup path, timestamp, and device information for each backup.
3. IF module installation or verification fails, the application SHALL disable the module and restore the latest backups.
4. WHEN a user requests restoration, the application SHALL restore the latest backup for each original path and disable the overlay module.
5. The application SHALL check free space and font readability before creating a module.

### Requirement 9: Module Export and Installation

**User Story:** As a user, I want a valid module ZIP, so that I can install the font module with a supported Root manager.

#### Acceptance Criteria

1. WHEN a module directory is generated, the application SHALL export `GlobalFont_<font>.zip` with the Magisk module hierarchy.
2. The exported ZIP SHALL preserve executable permissions for shell scripts and readable permissions for font files.
3. WHEN export completes, the application SHALL reopen the ZIP and validate required entries and `module.prop` values.
4. The application SHALL provide a provider-specific install intent and a share or file-open fallback.

### Requirement 10: Root Audit Log

**User Story:** As a user, I want an operation log, so that I can inspect Root command results.

#### Acceptance Criteria

1. The application SHALL record each Root command, timestamp, exit code, and error output.
2. The Root management area SHALL provide a log page ordered by newest operation.
3. WHEN an operation finishes, the UI SHALL display the result returned by the Root engine.

### Requirement 11: MIUI and HyperOS Adaptation

**User Story:** As a Xiaomi device user, I want the application to understand the installed font configuration, so that the selected overlay follows the device's actual font priority.

#### Acceptance Criteria

1. The application SHALL inspect existing font files under system, product, system extension, and vendor font directories.
2. The application SHALL read available `fonts.xml` and `font_fallback.xml` files according to configuration priority.
3. The application SHALL inspect MIUI, HyperOS, and incremental build properties.
4. WHEN a replacement module is generated, the application SHALL select targets from the analyzed profile and include relevant configuration overlays.

### Requirement 12: Compatibility Report

**User Story:** As a user, I want a compatibility result before applying a font, so that missing Chinese, English, or numeric glyphs are visible.

#### Acceptance Criteria

1. The application SHALL check Chinese, English, and numeric glyph coverage using the imported font file.
2. The application SHALL classify the font as full support, partial support, or not recommended.
3. The application SHALL export a report containing device information, detected paths, selected strategy, and failure reason.

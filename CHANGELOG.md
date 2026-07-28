# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Removed Dagger from the plugin, CLI, and core library; the object graph is created with plain factory code. This eliminates `NoSuchMethodError: dagger.internal.Preconditions.checkNotNullFromProvides` conflicts with other build tools on the buildscript classpath
- Dependency resolution moved from execution-time Project traversal to Gradle's ArtifactView API: the plugin now supports the configuration cache (`--no-configuration-cache` is no longer required), classifies modules vs libraries through component identifiers, and no longer double counts duplicate library versions

## [0.1.0-alpha05] - 2026-07-25
Maintenance release for projects on AGP 8.x, cut from the `support/agp-8.x` branch. Ships the same feature set as the modern line (including everything in Unreleased above) built against AGP 8.13.2.

### Added
- Configuration cache support (`--no-configuration-cache` is no longer required; configuration on demand is still not supported)

### Changed
- Removed Dagger from the plugin, CLI, and core library (fixes `NoSuchMethodError: dagger.internal.Preconditions.checkNotNullFromProvides` conflicts)
- Dependency resolution rewritten on Gradle's ArtifactView API; duplicate library versions are no longer double counted
- Requires AGP 8.13.2+, Gradle 8.13+, and JDK 17

[0.1.0-alpha05]:
https://github.com/grab/app-sizer/releases/tag/v0.1.0-alpha05

## [0.2.0-alpha01] - 2026-07-21
First release of the 0.2.x line, targeting modern Android toolchains.

### Added
- Support for Gradle 9 and Android Gradle Plugin 9

### Changed
- Migrated off AGP APIs removed in AGP 9 (legacy Variant API, `CommonExtension` type parameters); variants are matched from the DSL and the new `androidComponents` API
- Requires AGP 9.0+, Gradle 9.1+, and JDK 17. Projects on AGP 8.x should use the `0.1.x` releases

[0.2.0-alpha01]:
https://github.com/grab/app-sizer/releases/tag/v0.2.0-alpha01

## [0.1.0-alpha03] - 2025-11-17
Stability release.

### Fixed
- Prevented build crashes when the plugin encounters projects with incompatible or missing build variant configurations
- Prevented configuration failures by handling task-manager errors defensively

### Changed
- Updated publishing to the new Central Portal URLs

[0.1.0-alpha03]:
https://github.com/grab/app-sizer/releases/tag/v0.1.0-alpha03

## [0.1.0-alpha02] - 2025-04-03
Second alpha release of App Sizer with Kotlin Multiplatform support and bug fixes.

### Added
- Support for Kotlin Multiplatform (KMP) modules

### Fixed
- Fixed APK total size calculation
- Fixed report generation for default device specifications
- Improved temporary file handling by generating default_device to build folder instead of temp file

### Changed
- Updated sample build script
- Improved documentation and configuration options

[0.1.0-alpha02]: 
https://github.com/grab/app-sizer/releases/tag/0.1.0-alpha02
https://github.com/grab/app-sizer/releases/tag/v0.1.0-alpha02

## [0.1.0-alpha01] - 2024-11-29
Initial release of App Sizer as an open-source project.

### Added
- Gradle plugin for analyzing Android app download sizes
- Command-line tool for non-Gradle build systems
- Support for analyzing:
    - Total app download size
    - Detailed size breakdown
    - Size contribution by teams
    - Module-wise size contribution
    - Size contribution by libraries
    - List of large files
- Reports in multiple formats:
    - InfluxDB database (1.x)
    - Markdown tables
    - JSON data
- Comprehensive documentation
    - Usage guides for both Gradle plugin and CLI tool
    - Docker setup for InfluxDB & Grafana
    - Detailed configuration options
    - Known limitations documentation

### Notes
- This is an alpha release for early feedback
- API and configuration options may change in future releases
- Testing with different Android project configurations is ongoing

[0.1.0-alpha01]: https://github.com/grab/app-sizer/releases/tag/0.1.0-alpha01
# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
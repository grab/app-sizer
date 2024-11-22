# App Sizer

## Overview
App Sizer is a tool designed to analyze the download size of Android applications. By providing detailed insights into the composition of your app's binary, App Sizer helps developers identify areas for size reduction, ultimately improving user acquisition and retention rates.

*The app download size in Android refers to the amount of data a user needs to download from an app store (typically Google Play Store) to install an application on their Android device*

<p align="center">
<img src="./images/dashboard.gif" width="90%">
</p>

## Key Features
App Sizer offers comprehensive analysis including:
1. Total app download size
2. Detailed size breakdown
3. Size contribution by teams
4. Module-wise size contribution
5. Size contribution by libraries
6. List of large files

Reports are generated based on the provided Android device specifications. Our [blogpost][blog-post] introduce the tool features

## Quick Start

App Sizer provides two flexible integration methods:

* A Gradle plugin that seamlessly integrates with your Android Gradle project.
* A command-line tool to cater to non-Gradle build systems, offering the same comprehensive features.

  *Note: The command-line option was the original implementation and remains supported for broader compatibility.*

### Gradle Plugin Integration
In root `build.gradle`:

```groovy
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath "com.grab:app-sizer:SNAPSHOT"
    }
}
```
In the app module 's `build.gradle`
```groovy
apply plugin: "com.grab.app-sizer"

// AppSizer configuration
appSizer {
    // DSL
}
```

To run analysis, execute

```
./gradlew app:appSizeAnalysisRelease --no-configure-on-demand --no-configuration-cache
```

For plugin configuration options, see [Plugin Configuration][plugin_doc].


### Cli Tool Integration

1. Download the latest `clt-all.jar` from [Releases](link-to-releases)
2. Ensure Java 11+ is installed

To run analysis using the command line tool, execute
```text
java -jar clt-all.jar --config-file ./path/to/config/app-size-settings.yml
```

For command line configuration options, see [Commandline Configuration][cli_doc].

## Report Types

App Sizer currently supports three types of reports:

* InfluxDB database (1.x) - suitable for CI tracking and enabling the creation of customized dashboards. For InfluxDB and Grafana setup, see our [Docker Setup Guide][grafana-docker].
* Markdown table for convenient local analysis.
* JSON data for compatibility with other platforms.

*The Markdown & Json reports are saved as [option]-report.md in the configured output folder (default: app/build/sizer/reports)*

For more detail on reports, see [Report Detail][report_doc]

## How it works
App Sizer functions as a mapping tool to generate the report. It takes APK, AAR, and JAR files as inputs.
1. **Input parsing**:
- The tool parses the APK down to file and class levels. It calculates the contribution of each component to the total app download size.
- Similarly, App Sizer parses AAR and JAR files.
2. **Mapping and Report Generation**:
- The tool then maps the APK components to their corresponding elements in the AAR and JAR files.
- Based on this analysis and other metadata, App Sizer generates comprehensive reports detailing size contributions.

## Limitations

App Sizer approximates class download sizes due to Dex structure complexity, and may not accurately attribute sizes for inline functions or uncategorized files. Results should be interpreted as close estimates, best used for identifying trends and relative size comparisons rather than exact measurements.

For more details on limitations, see the [Limitation][limitation_doc].

## Components
* [Gradle Plugin][gradle-plugin]
* [Command line tool][commandline-tool]
* [InfluxDb & Grafana Docker][grafana-docker]

## Contributing

If you find any issues or have suggestions for improvements, please open an issue or submit a pull request to the App Sizer repository.

## License

```
MIT License


Copyright 2024 Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.


Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:


The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.


THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE
```

[report_doc]: ./report.md
[plugin_doc]: ./plugin.md
[cli_doc]: ./cli.md
[limitation_doc]:./limitation.md
[gradle-plugin]: ../gradle-plugin
[commandline-tool]: ../clt
[grafana-docker]: ../docker
[blog-post]: https://engineering.grab.com/project-bonsai




# App Sizer

## Overview
App Sizer is a tool designed to analyze the download size of Android applications. By providing detailed insights into the composition of your app's binary, App Sizer helps developers identify areas for size reduction, ultimately improving user acquisition and retention rates.

*The app download size in Android refers to the amount of data a user needs to download from an app store (typically Google Play Store) to install an application on their Android device*

<p align="center">
<img src="./images/dashboard.gif" width="95%">
</p>

## Key Features

App Sizer offers comprehensive analysis including:

1. Total app download size
2. Detailed size breakdown
3. Size contribution by teams
4. Module-wise size contribution
5. Size contribution by libraries
6. List of large files

Reports are generated based on the provided Android device specifications. Our [blogpost][blog_post] introduce the tool features

## Integration

App Sizer provides two flexible integration methods:

* A Gradle plugin that seamlessly integrates with your Android Gradle project ([Plugin Configuration Detail][plugin_doc].)
* A command-line tool to cater to non-Gradle build systems, offering the same comprehensive features ([Commandline Configuration Detail][cli_doc].)

  *Note: The command-line option was the original implementation and remains supported for broader compatibility.*

## Report types

App Sizer currently supports three types of reports:

* InfluxDB database (1.x) - It is suitable for CI tracking and enabling the creation of customized dashboards (with visualization tools like Grafana). We provide an InfluxDB and Grafana setup; see our [Docker Setup Guide][grafana_docker_doc].
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
* [Gradle Plugin][gradle_plugin]
* [Command line tool][commandline_tool]
* [InfluxDb & Grafana Docker][grafana_docker]

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
[grafana_docker_doc]: ./docker.md
[limitation_doc]:./limitation.md
[gradle_plugin]: https://github.com/grab/app-sizer/tree/master/sizer-gradle-plugin
[commandline_tool]: https://github.com/grab/app-sizer/tree/master/cli
[grafana_docker]: https://github.com/grab/app-sizer/tree/master/docker
[blog_post]: https://engineering.grab.com/project-bonsai
[latest_release_link]: https://github.com/grab/app-sizer/releases




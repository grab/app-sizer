App Sizer
======

See the [project website][app-sizer] for documentation and APIs.

The download size of an Android app is a pivotal factor impacting user acquisition and retention - smaller apps are more likely to be downloaded and less likely to be uninstalled. 
Understanding the composition of your app's download size can help optimize this key metric.

App Sizer is a tool designed to dissect your app's binary and provide a comprehensive analysis of its size contributors. 
With the insights provided by App Sizer, you can identify specific areas for size reduction, prioritize your action items effectively, and make strategic decisions that help reduce your app's download size. 

App Sizer offers two user-friendly options for integration:

* A Gradle plugin that seamlessly integrates with your Android Gradle project.
* A command-line tool to cater to non-Gradle build systems, offering the same comprehensive features. 
A bit of history, the tool was built as a commandline tool at the beginning, and we would like to maintain this option.

Presently, App Sizer supports three types of reports:

* Markdown table for convenient local analysis.
* InfluxDB database - It's suitable for your CI tracking and it's enabling the creation of a customized dashboard.
* JSON data for compatibility with other platforms.

We are plan working on expanding our database support to facilitate a wider range of analytics in the near future.


Components
---------
* [Gradle Plugin][gradle-plugin]
* [Command line tool][commandline-tool]
* [InfluxDb & Grafana Docker][grafana-docker]

Features
---------

How it works
------------
In simple words, it's just a mapping tool. App Sizer use the APKs, AAR & JARs files as the inputs.
The tool just parse the inputs to files/classes level components. And trying to map them from the APKs to the parsed data from AAR & jars files.
Thank for the Google Android opensource project,

Requirement
---------

Limitation
---------

Resources
---------

License
-------

```
Copyright 2024 Grabtaxi Holdings PTE LTD (GRAB)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


[app-sizer]: TBA
[gradle-plugin]: TBA
[commandline-tool]: TBA
[grafana-docker]: TBA

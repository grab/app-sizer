# Gradle Tasks

The app-sizer-plugin does not do any major configuration during Gradle's `Configuration` phase. Most of the work for analysis is moved to
execution phase via the following Gradle tasks.

All the tasks are available under task group `sizer`.

## Tasks

### appSizeAnalysis[VariantName]

`appSizeAnalysis` is the main task that calling API from "app-sizer" module


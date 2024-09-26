# App Sizer Plugin
App Sizer provide the app sizer gradle plugin as the option to seamlessly integrates with your Android Gradle project. This option is recommended.

## Getting Started

1. Add the plugin to your root `build.gradle`

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

2. Apply the plugin in your app module's `build.gradle`

```groovy
apply plugin: "com.grab.app-sizer"

appSizer {
    // Configuration goes here
}
```

3. Run the analysis

```bash
./gradlew app:appSizeAnalysisRelease --no-configure-on-demand --no-configuration-cache
```

## Configuration
Use the registered `appSizer` extension block to the app module's `build.gradle` to configure App Sizer Plugin

```groovy
appSizer {
    enabled = true
    projectInput {
        // config the input for the plugin
    }
    metrics {
        // config the output for the plugin
    }
}
```
* **enabled**: Given the App Sizer Plugin has not supported configuration on demand & configuration catching. We provide you an option to turned of the plugin just in case it impact your gradle configuration performance.

### Project Input
Configure the input for the project:

```groovy
appSizer {
    projectInput {
        largeFileThreshold = 10
        teamMappingFile = file("${rootProject.rootDir}/module-owner.yml")
        enableMatchDebugVariant = true
        variantFilter { variant ->
            variant.setIgnore(variant.flavors.contains("ignore-flavor"))
        }
        apk {
            // APK Generation
        }
    }
    ...
}
```

| Property | Description                                                    |
|----------|----------------------------------------------------------------|
| `largeFileThreshold` | File size threshold (in bytes) for considering a file as large. |
| `teamMappingFile` | YAML file mapping project modules to team owners.              |
| `enableMatchDebugVariant` | If true, uses debug AAR files to improve build performance.    |
| `variantFilter` | Specifies which variants to exclude from analysis.             |

And example of `teamMappingFile`:

```yaml
Platform:
  - app
Team1:
  - android-module-level1
  - kotlin-module
Team2:
  - sample-group:android-module-level2
```

### APK Generation

Configure APK generation settings:

```groovy
appSizer {
    projectInput {
        ...
        apk {
            deviceSpecs = [
                file("${rootProject.rootDir}/app-size-config/device-1.json"),
                file("${rootProject.rootDir}/app-size-config/device-2.json")
            ]
            bundleToolFile = file("${rootProject.rootDir}/binary/bundletool-all-1.15.4.jar")
        }
    }
    ...
}
```

| Property | Description                                                                                                                |
|----------|----------------------------------------------------------------------------------------------------------------------------|
| `deviceSpecs` | List of [device specification](https://developer.android.com/tools/bundletool#generate_use_json) files for APK generation. |
| `bundleToolFile` | Path to the [bundletool](https://github.com/google/bundletool) JAR file.                                                   |

### Output Configuration

Configure the reporting output:

```groovy
appSizer {
    ...
    metrics {
        influxDB {
            dbName = "sizer"
            reportTableName = "app_size"
            url = "http://localhost:8086"
            username = "db-username"
            password = "db-pw"
        }
        local {
            outputDirectory = project.layout.buildDirectory.dir("app-sizer")
        }
        customAttributes.putAll(
            ["pipeline_id": "1001"]
        )
    }
}
```

| Property | Description                                                                       |
|----------|-----------------------------------------------------------------------------------|
| `local.outputDirectory` | Directory to save markdown and JSON reports (default is `app/build/sizer/reports`)|
| `customAttributes` | Map of additional attributes to include in every report row.                      |

#### InfluxDB Configuration

| Property | Description                                         |
|----------|-----------------------------------------------------|
| `dbName` | Name of the InfluxDB database.                      |
| `reportTableName` | Measurement name for storing report data.           |
| `url` | URL of the InfluxDB server.                         |
| `username` | InfluxDB username (optional).                       |
| `password` | InfluxDB password (optional).                       |
| `retentionPolicy` | InfluxDB retention policy configuration (optional). |

## Full Configuration Example

```groovy
appSizer {
    enabled = true
    projectInput {
        apk {
            bundleToolFile = file("${rootProject.rootDir}/binary/bundletool-all-1.15.4.jar")
            deviceSpecs = [
                file("${rootProject.rootDir}/app-size-config/device-1.json"),
                file("${rootProject.rootDir}/app-size-config/device-2.json")
            ]
        }
        variantFilter { variant ->
            variant.setIgnore(variant.flavors.contains("gea"))
        }
        enableMatchDebugVariant = true
        largeFileThreshold = 10
        teamMappingFile = file("${rootProject.rootDir}/module-owner.yml")
    }
    metrics {
        influxDB {
            dbName = "sizer"
            reportTableName = "app_size"
            url = "http://localhost:8086"
            username = "root"
            password = "root"
            retentionPolicy {
                name = "app_sizer"
                duration = "360d"
                shardDuration = "0m"
                replicationFactor = 2
                setAsDefault = true
            }
        }
        local {
            outputDirectory = project.layout.buildDirectory.dir("app-sizer")
        }
        customAttributes.putAll(
            ["pipeline_id": "1001"]
        )
    }
}
```

## Task Graph

<p align="center">
<img src="images/task-graph.png" width="80%">
</p>

## Troubleshooting

- If you encounter issues with the `verifyResourceRelease` task, try enabling `enableMatchDebugVariant`.
- Ensure that the `bundletool` JAR file is correctly referenced in your configuration.

## Resources

- [Bundletool GitHub Repository](https://github.com/google/bundletool)
- [InfluxDB Documentation](https://www.influxdata.com/time-series-platform/)

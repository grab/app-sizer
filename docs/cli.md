# App Sizer CLI

App Sizer provides a Command Line Interface (CLI) to cater to non-Gradle build systems, offering the same comprehensive features as the Gradle plugin.


## Getting Started

1. Generate the command line binary file:
   ```
   ./gradlew clt:shadowJar
   ```

2. Create your config file following [this template](../cli-config-template.yml).

3. Run the analysis using the command line tool:
   ```
   java -jar clt-all.jar --config-file ./path/to/config/your-config-file.yml
   ```

## Configuration

The App Sizer CLI accepts a YAML file as configuration ([template](../cli-config-template.yml)). The file consists of three main blocks:

```yaml
project-input:
  # Configure the input for the project
apk-generation:
  # APK Generation configuration
report:
  # Output Configuration
```

### Project Input

| Property | Description                                                                                                                                                                                                                                                                                                                                |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| libraries-directory | Path to all SDK & library binaries that your project depends on. In Gradle, you can save all dependencies in a folder by setting a new Gradle home path for your build with the `-g` option. Example: `./gradlew assembleRelease -g ./new-gradle`. Then all dependency binaries will be saved to `./new-gradle/caches/modules-2/files-2.1` |
| modules-directory | Path to all of your modules' AAR & Jar files. In Gradle, you can run `assembleDebug` in the project root folder to build all AAR/Jar files, then set the root folder for this property                                                                                                                                                     |
| modules-dir-is-project-root | Boolean value. Enable this flag if you set the root project as the modules-directory to optimize performance                                                                                                                                                                                                                               |
| r8-mapping-file | Path to the R8 mapping file if you enable R8 for your build                                                                                                                                                                                                                                                                                |
| owner-mapping-file | Path to YAML file mapping project modules to team owners                                                                                                                                                                                                                                                                                   |
| version | Your app version                                                                                                                                                                                                                                                                                                                           |
| large-file-threshold | File size threshold (in bytes) for considering a file as large                                                                                                                                                                                                                                                                             |
| project-name | Project name                                                                                                                                                                                                                                                                                                                               |

And example of `owner-mapping-file`:

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

| Property | Description |
|----------|-------------|
| bundle-tool | Path to the [bundletool](https://github.com/google/bundletool) JAR file |
| app-bundle-file | Path to the app bundle file (aab) |
| device-specs | List of [device specification](https://developer.android.com/tools/bundletool#generate_use_json) files for APK generation |
| key-signing | Key signing information |

### Output Configuration

| Property | Description |
|----------|-------------|
| output-directory | Directory to save markdown and JSON reports |
| custom-attributes | Map of additional attributes to include in every report row |
| influx-db-config | InfluxDB configuration (see below) |

#### InfluxDB Configuration

| Property | Description |
|----------|-------------|
| url | URL of the InfluxDB server |
| db-name | Name of the InfluxDB database |
| report-table-name | Measurement name for storing report data |
| username | InfluxDB username (optional) |
| password | InfluxDB password (optional) |
| retention-policy | InfluxDB retention policy configuration (optional) |

## Full Configuration Example

```yaml
project-input:
  libraries-directory: "./build/gradle-cache/caches/modules-2/files-2.1"
  modules-directory: "./"
  modules-dir-is-project-root: true
  r8-mapping-file: "./app/build/outputs/mapping/proDebug/mapping.txt"
  owner-mapping-file: "./module-owner.yml"
  version: "1.0.1"
  large-file-threshold: 10
  project-name: "sample"
apk-generation:
  bundle-tool: "./binary/bundletool-all-1.15.4.jar"
  app-bundle-file: "./app/build/outputs/bundle/proDebug/sample-bundle-file-pro-debug.aab"
  device-specs:
    - "./app-size-config/device-1.json"
    - "./app-size-config/device-2.json"
  key-signing:
    keystore-file: "./buildsystem/sample-release.keystore"
    keystore-pw: "12345678"
    key-alias: "key0"
    key-pw: "12345678"
report:
  output-directory: "./build/app-sizer"
  custom-attributes:
    pipelineId: "100"
  influx-db-config:
    db-name: "sizer"
    url: "http://localhost:8086"
    username: "root"
    password: "root"
    report-table-name: "app_size"
    retention-policy:
      name: "app_sizer"
      duration: "360d"
      shard-duration: "0m"
      replication-factor: 2
      is-default: true
```


## Resources

- [Bundletool GitHub Repository](https://github.com/google/bundletool)
- [InfluxDB Documentation](https://www.influxdata.com/time-series-platform/)
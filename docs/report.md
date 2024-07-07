# Reports

App Sizer supports three types of reports to cater to different use cases and environments:

1. InfluxDB database (1.x)
2. Markdown tables
3. JSON data

## InfluxDB Database

InfluxDB (1.x) is recommended for CI tracking and creating customized dashboards. It's ideal for integrating the tool into your CI pipeline to track historical reports between releases.

### Setup

We provide a Docker image with InfluxDB (1.x) and Grafana pre-configured:

```sh
docker run -d \
  --name sizer-influxdb-grafana \
  -p 3003:3003 \
  -p 3004:8083 \
  -p 8086:8086 \
  -v /path/for/influxdb:/var/lib/influxdb \
  -v /path/for/grafana:/var/lib/grafana \
  mikenguyen/sizer-influx-grafana:latest
```

For more details on the Docker setup, see our [Docker guide][grafana-docker].

### Dashboard

A default **App Download Size Breakdown** dashboard is included in the Grafana docker instance. If you have an existing InfluxDB and Grafana setup, you can import our dashboard using this [JSON file](../grafana/dashboard-to-import.json).

## Markdown Tables

Markdown tables provide a convenient format for local analysis. The report is saved as `[option]-report.md` in the configured output folder (default: `app/build/sizer/reports`).

### Example: Module-wise Size Contribution

| Contributor | Owner | Size | 
|-------------|-------|------|
| app | Platform | 90.078 KB | 
| android-module-level2 | Team2 | 123.968 KB | 
| android-module-level1 | Team1 | 124.042 KB | 
| kotlin-module | Team2 | 248.326 KB | 

## JSON Report

JSON reports offer compatibility with other platforms and tools. The report is saved as `[option]-metrics.json` in the configured output folder.

### JSON Structure

Here's a sample of the JSON structure:

```json
[
  {
    "name": "apk",
    "fields": [
      {
        "name": "size",
        "value": "1789199",
        "value_type": "integer"
      },
      {
        "name": "pipeline_id",
        "value": "1001",
        "value_type": "string"
      }
    ],
    "tags": [
      {
        "name": "contributor",
        "value": "apk",
        "value_type": "string"
      },
      {
        "name": "project",
        "value": "sample",
        "value_type": "string"
      },
      {
        "name": "app_version",
        "value": "1.0.9",
        "value_type": "string"
      },
      {
        "name": "build_type",
        "value": "proDebug",
        "value_type": "string"
      },
      {
        "name": "device_name",
        "value": "device-1",
        "value_type": "string"
      }
    ],
    "timestamp": 1720248703061
  },
  // More measurements...
]
```

### JSON Fields Explanation

Each object in the array represents a single database row and contains the following properties:

1. `name`: The measurement name (e.g., "apk")
2. `fields`: An array of fields containing numerical or custom data
3. `tags`: Each measurement includes relevant tags such as the project name, app version, build type, and device name, allowing for detailed analysis and filtering of the data.
4. `timestamp`: Unix timestamp (in milliseconds) when the measurement was taken

### Using the JSON Report

The JSON format makes it easy to:

1. Import the data into various tools/databases
2. Integrate with other CI/CD processes
3. Perform programmatic analysis of app size trends over time

You can parse this JSON data using any standard JSON library in your preferred programming language to extract and analyze the information as needed for your project.

## Customizing Reports

You can customize the reports by modifying the configuration in your Gradle plugin or CLI tool setup. For more details, refer to the [Plugin Configuration][plugin_doc] or [CLI Configuration][cli_doc] guides.

[grafana-docker]: ../docker
[grafana-dashboard]: ../grafana/dashboard-to-import.json
[plugin_doc]: ./plugin.md
[cli_doc]: ./cli.md



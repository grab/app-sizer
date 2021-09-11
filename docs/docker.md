# Sizer-influx-grafana

![Grafana][grafana-version] ![Influx][influx-version]

This is a Docker image based on the awesome [docker-influxdb-grafana](https://github.com/philhawthorne/docker-influxdb-grafana) from [Phil Hawthorne](https://github.com/philhawthorne).

## Key Different

- Newer Grafana version
- Added provisioned app-sizer Dashboard and Datasources
- ChronoGraf is not included in this container

The main purpose of this image is to be used to demo data from [App Sizer][app-sizer-page].

| Component | Version |
|-----------|---------|
| InfluxDB  | 1.8.2   |
| Grafana   | 9.0.0   |

## Pre-configured Dashboard

The Docker image includes a pre-configured dashboard in Grafana:

<p align="center">
<img src="../docs/images/dashboard.gif" width="95%">
</p>

To use the dashboard, you either to:
- **Default Configuration**: Uses database name `sizer` and measurement (table) named `app_size`. (They are default values configured in the App Sizer tool)
- **Custom Configuration**:
  - For a different database name: Update the [Grafana Data Sources](https://grafana.com/docs/grafana/latest/datasources/) named InfluxDB.
  - For a different measurement name: Update all [queries](https://grafana.com/docs/grafana/latest/panels-visualizations/query-transform-data/) and [variables](https://grafana.com/docs/grafana/latest/dashboards/variables/) in the dashboard.

To import the dashboard into an existing setup:
1. Use this [JSON file][json-dashboard-file].
2. Ensure you add the proper Grafana datasource.
3. Update the measurement (table) name in the queries and variables if necessary

## Quick Start

To start the container with persistence, you can use our teammate's docker image exported to Docker Hub:

```sh
docker run -d \
  --name sizer-influxdb-grafana \
  -p 3003:3003 \
  -p 8086:8086 \
  -v /path/for/influxdb:/var/lib/influxdb \
  -v /path/for/grafana:/var/lib/grafana \
  mikenguyen/sizer-influx-grafana:latest
```

## Mapped Ports

| Host | Container | Service |
|------|-----------|---------|
| 3003 | 3003      | Grafana |
| 8086 | 8086      | InfluxDB|

## Accessing Services

### Grafana

- URL: [http://localhost:3003](http://localhost:3003)
- Username: `root`
- Password: `root`

### InfluxDB

- Port: 8086
- Username: `root`
- Password: `root`


[json-dashboard-file]: ../grafana/dashboard-to-import.json
[app-sizer-page]: ./index.md
[grafana-version]: https://img.shields.io/badge/Grafana-9.0.0-brightgreen
[influx-version]: https://img.shields.io/badge/Influx-1.8.2-brightgreen




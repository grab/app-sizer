# Sizer-influx-grafana

![Grafana][grafana-version] ![Influx][influx-version]

This is a Docker image based on the awesome [docker-influxdb-grafana](https://github.com/philhawthorne/docker-influxdb-grafana) from [Phil Hawthorne](https://github.com/philhawthorne).

## Key Features

- Newer Grafana version
- Added provisioned app-sizer Dashboard and Datasources
- ChronoGraf is not included in this container

The main purpose of this image is to be used to demo data from [App Sizer](tobe-updated).

| Component | Version |
|-----------|---------|
| InfluxDB  | 1.8.2   |
| Grafana   | 9.0.0   |

## Quick Start

To start the container with persistence, you can use our teammate's docker image exported to Docker Hub:

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

[grafana-version]: https://img.shields.io/badge/Grafana-9.0.0-brightgreen
[influx-version]: https://img.shields.io/badge/Influx-1.8.2-brightgreen
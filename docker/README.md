# Sizer-influx-grafana

![Grafana][grafana-version] ![Influx][influx-version]


This is a Docker image based on the awesome [docker-influxdb-grafana](https://github.com/philhawthorne/docker-influxdb-grafana) from [
Phil Hawthorne](https://github.com/philhawthorne).

The main point of difference with this image is:

* Newer Grafana version
* Adding provisioned app-sizer DashBoard and Datasources 
* ChronoGraf is not included in this container

The main purpose of this image is to be used to show data from a [App Sizer](tobe-updated). 

| Description  | Value |
|--------------|-------|
| InfluxDB     | 1.8.2 |
| Grafana      | 9.0.0 |

## Quick Start

To start the container with persistence you can use the following:

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

To stop the container launch:

```sh
docker stop docker-influxdb-grafana
```

To start the container again launch:

```sh
docker start docker-influxdb-grafana
```

## Mapped Ports

```
Host		Container		Service

3003		3003			grafana
8086		8086			influxdb
```

## Grafana

Open <http://localhost:3003>

```
Username: root
Password: root
```

## InfluxDB
```
Username: root
Password: root
Port: 8086
```


[grafana-version]: https://img.shields.io/badge/Grafana-9.0.0-brightgreen
[influx-version]: https://img.shields.io/badge/Influx-1.8.2-brightgreen

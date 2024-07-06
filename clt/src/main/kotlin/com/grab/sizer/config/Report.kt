package com.grab.sizer.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.File

data class ReportConfig(
    @JsonProperty("output-directory") private val outputDirectoryPath: String,
    @JsonProperty("custom-attributes") val customAttributes: Map<String, String>?,
    @JsonProperty("influx-db-config") val influxDbConfig: InfluxDbConfig?
) {
    @get:JsonIgnore
    val outputDirectory: File
        get() = File(outputDirectoryPath)
}

data class InfluxDbConfig(
    @JsonProperty("retention-policy") val retentionPolicy: RetentionPolicy?,
    @JsonProperty("report-table-name") val reportTableName: String?,
    @JsonProperty("db-name") val dbName: String?,
    val url: String,
    val username: String,
    val password: String,
)

data class RetentionPolicy(
    val name: String,
    val duration: String,
    @JsonProperty("shard-duration") val shardDuration: String,
    @JsonProperty("replication-factor") val replicationFactor: Int,
    @JsonProperty("is-default") val isDefault: Boolean
)
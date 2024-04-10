package com.grab.sizer.report.db

import com.grab.sizer.report.DefaultField
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.Report
import com.grab.sizer.report.TagField
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.influxdb.impl.Preconditions
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SHOW_DATABASE_COMMAND = "SHOW DATABASES"

data class InfluxDBConfig(
    val dbName: String,
    val url: String,
    val username: String?,
    val password: String?,
    val databaseRetentionPolicy: DatabaseRetentionPolicy
)

data class DatabaseRetentionPolicy(
    val name: String,
    val duration: String,
    val shardDuration: String,
    val replicationFactor: Int,
    val isDefault: Boolean,
)

class InfluxDBFactory {
    fun create(config: InfluxDBConfig): InfluxDB {
        return org.influxdb.InfluxDBFactory.connect(config.url, config.username, config.password).apply {
            setLogLevel(InfluxDB.LogLevel.FULL)
            enableBatch(
                BatchOptions.DEFAULTS
                    .threadFactory { runnable: Runnable? ->
                        val thread = Thread(runnable)
                        thread.setDaemon(true)
                        thread
                    }
            )
            Runtime.getRuntime().addShutdownHook(Thread(::close))
        }
    }
}

class InfluxDbReportDao @Inject constructor(
    private val influxDB: InfluxDB,
    private val config: InfluxDBConfig
) : ReportDao {
    init {

        if (databaseExists(config.dbName)) {
            influxDB.setDatabase(config.dbName)
        } else {
            createDatabase(config)
            influxDB.setDatabase(config.dbName)
        }
    }

    private fun createDatabase(influxDBConfig: InfluxDBConfig) {
        Preconditions.checkNonEmptyString(influxDBConfig.dbName, "name")
        influxDB.query(
            Query(
                "CREATE DATABASE ${influxDBConfig.dbName}"
            )
        )

        val retentionPolicy = influxDBConfig.databaseRetentionPolicy

        influxDB.query(
            Query(
                """CREATE RETENTION POLICY ${retentionPolicy.name} 
                    |ON ${influxDBConfig.dbName} 
                    |DURATION ${retentionPolicy.duration} 
                    |REPLICATION ${retentionPolicy.replicationFactor} 
                    |SHARD DURATION ${retentionPolicy.shardDuration} 
                    |${if (retentionPolicy.isDefault) "DEFAULT" else ""}
                    |""".trimMargin()
            )
        )
    }

    private fun describeDatabases(): List<String> {
        val result: QueryResult = influxDB.query(Query(SHOW_DATABASE_COMMAND))
        // {"results":[{"series":[{"name":"databases","columns":["name"],"values":[["mydb"]]}]}]}
        // Series [name=databases, columns=[name], values=[[mydb], [unittest_1433605300968]]]
        val databaseNames = result.results[0].series[0].values
        val databases: MutableList<String> = ArrayList()
        if (databaseNames != null) {
            for (database in databaseNames) {
                databases.add(database[0].toString())
            }
        }
        return databases
    }

    private fun databaseExists(name: String): Boolean {
        val databases = describeDatabases()
        for (databaseName in databases) {
            if (databaseName.trim { it <= ' ' } == name) {
                return true
            }
        }
        return false
    }

    override fun addReport(report: Report) {
        val pointsBuilder = BatchPoints.builder()
        report.rows.forEach { row ->
            val point = Point.measurement(report.id)
                .apply {
                    time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    val allFields = row.fields + report.projectInfo.toCommonTags() + report.customPropertiesToTags()

                    allFields.forEach { field ->
                        when (field) {
                            is DefaultField -> {
                                when (val value = field.value) {
                                    is Long -> addField(field.name, value)
                                    is Int -> addField(field.name, value)
                                    is Boolean -> addField(field.name, value)
                                    is Double -> addField(field.name, value)
                                    is Short -> addField(field.name, value)
                                    is Float -> addField(field.name, value)
                                    else -> addField(field.name, value.toString())
                                }
                            }

                            is TagField -> {
                                tag(field.name, field.value.toString())
                            }
                        }
                    }
                }.build()
            pointsBuilder.point(point)
        }
        influxDB.write(pointsBuilder.build())
    }

    private fun Report.customPropertiesToTags(): List<TagField> = customProperties.map { property ->
        TagField(property.key, property.value)
    }

    private fun ProjectInfo.toCommonTags(): List<TagField> =
        listOf(
            TagField(
                name = "project",
                value = projectName,
            ),
            TagField(
                name = "app_version",
                value = versionName,
            ),
            TagField(
                name = "build_type",
                value = buildType,
            ),
            TagField(
                name = "device_name",
                value = deviceName,
            )
        )

    override fun getReportById(id: String) {
        TODO("Not yet implemented")
    }
}
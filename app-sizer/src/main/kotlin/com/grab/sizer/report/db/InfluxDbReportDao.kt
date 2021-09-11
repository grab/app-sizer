/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.sizer.report.db

import com.grab.sizer.report.DefaultField
import com.grab.sizer.report.Report
import com.grab.sizer.report.TagField
import org.influxdb.BatchOptions
import org.influxdb.InfluxDB
import org.influxdb.dto.BatchPoints
import org.influxdb.dto.Point
import org.influxdb.dto.Query
import org.influxdb.dto.QueryResult
import org.influxdb.impl.Preconditions
import java.io.Serializable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val SHOW_DATABASE_COMMAND = "SHOW DATABASES"
private const val DEFAULT_TABLE = "app_size"
private const val DEFAULT_DATABASE = "sizer"


data class InfluxDBConfig(
    private val dbName: String?,
    val url: String,
    val username: String?,
    val password: String?,
    val reportTableName: String?,
    val databaseRetentionPolicy: DatabaseRetentionPolicy?
) : Serializable {
    val databaseName: String = dbName ?: DEFAULT_DATABASE
}

data class DatabaseRetentionPolicy(
    val name: String,
    val duration: String,
    val shardDuration: String,
    val replicationFactor: Int,
    val isDefault: Boolean,
) : Serializable {
    companion object {
        fun createDefault() = DatabaseRetentionPolicy(
            name = "app_sizer",
            duration = "360d",
            shardDuration = "0m",
            replicationFactor = 2,
            isDefault = true
        )
    }
}

class InfluxDBFactory {
    fun create(config: InfluxDBConfig): InfluxDB {
        return org.influxdb.InfluxDBFactory.connect(config.url, config.username, config.password).apply {
            setLogLevel(InfluxDB.LogLevel.BASIC)
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

        if (databaseExists(config.databaseName)) {
            influxDB.setDatabase(config.databaseName)
        } else {
            createDatabase(config)
            influxDB.setDatabase(config.databaseName)
        }
    }

    private fun createDatabase(influxDBConfig: InfluxDBConfig) {
        Preconditions.checkNonEmptyString(influxDBConfig.databaseName, "name")
        influxDB.query(
            Query(
                "CREATE DATABASE ${influxDBConfig.databaseName}"
            )
        )

        influxDBConfig.databaseRetentionPolicy?.run {
            influxDB.query(
                Query(
                    """CREATE RETENTION POLICY $name 
                    |ON ${influxDBConfig.databaseName} 
                    |DURATION $duration 
                    |REPLICATION $replicationFactor 
                    |SHARD DURATION $shardDuration 
                    |${if (isDefault) "DEFAULT" else ""}
                    |""".trimMargin()
                )
            )
        }
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
        report.rows.forEachIndexed { index, row ->
            val point = Point.measurement(config.reportTableName ?: DEFAULT_TABLE)
                .apply {
                    /**
                     *  A small hack (add index to time) to prevent InfluxDb remove duplicate
                     *  (similar time + tags rows will be removed)
                     **/
                    time(System.currentTimeMillis() + index, TimeUnit.MILLISECONDS)
                    row.fields.forEach { field ->
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


    override fun getReportById(id: String) {
        TODO("Not yet implemented")
    }
}
package com.grab.sizer

import com.grab.sizer.config.ReportConfig
import com.grab.sizer.report.db.DatabaseRetentionPolicy
import com.grab.sizer.report.db.InfluxDBConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class CltOutputProvider(
    private val config: ReportConfig
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDBConfig? = config.influxDbConfig?.toSizerConfig()
    override fun provideOutPutDirectory(): File = config.outputDirectory
}

private fun com.grab.sizer.config.InfluxDbConfig.toSizerConfig(): InfluxDBConfig = InfluxDBConfig(
    dbName = dbName,
    url = url,
    username = username,
    password = password,
    databaseRetentionPolicy = retentionPolicy?.toSizerConfig() ?: DatabaseRetentionPolicy.createDefault()
)


private fun com.grab.sizer.config.RetentionPolicy.toSizerConfig(): DatabaseRetentionPolicy = DatabaseRetentionPolicy(
    name = name,
    duration = duration,
    shardDuration = shardDuration,
    replicationFactor = replicationFactor,
    isDefault = isDefault
)

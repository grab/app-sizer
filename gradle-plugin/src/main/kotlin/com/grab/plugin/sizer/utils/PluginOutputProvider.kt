package com.grab.plugin.sizer.utils

import com.grab.plugin.sizer.AppSizePluginExtension
import com.grab.plugin.sizer.configuration.InfluxDBExtension
import com.grab.plugin.sizer.configuration.RetentionPolicyExtension
import com.grab.sizer.report.db.DatabaseRetentionPolicy
import com.grab.sizer.report.db.InfluxDBConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class PluginOutputProvider(
    private val extension: AppSizePluginExtension,
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDBConfig? {
        val influxDBExtension = extension.metrics.influxDBExtension
        if (influxDBExtension.url.isPresent) {
            return influxDBExtension.toInfluxDBConfig()
        }
        return null
    }

    override fun provideOutPutDirectory(): File = extension.metrics.localExtension.outputDirectory.asFile.get()
}

private fun InfluxDBExtension.toInfluxDBConfig(): InfluxDBConfig = InfluxDBConfig(
    dbName = dbName.get(),
    url = url.get(),
    username = username.orNull,
    password = password.orNull,
    databaseRetentionPolicy = retentionPolicy.toDatabaseRetentionPolicy()
)

private fun RetentionPolicyExtension.toDatabaseRetentionPolicy(): DatabaseRetentionPolicy = DatabaseRetentionPolicy(
    name = name.get(),
    duration = duration.get(),
    shardDuration = shardDuration.get(),
    replicationFactor = replicationFactor.get(),
    isDefault = isDefault.get()
)
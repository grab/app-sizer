package com.grab.sizer

import com.grab.sizer.config.Config
import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.db.DatabaseRetentionPolicy
import com.grab.sizer.report.db.InfluxDBConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class CltOutputProvider(
    private val config: Config,
    private val deviceName: String
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDBConfig? = config.report.influxDbConfig?.toSizerConfig()
    override fun provideOutPutDirectory(): File = config.report.outputDirectory
    override fun provideProjectInfo(): ProjectInfo {
        return ProjectInfo(
            projectName = config.projectInput.projectName,
            versionName = config.projectInput.version,
            deviceName = deviceName
        )
    }

    override fun provideCustomProperties(): CustomProperties = config.report.customAttributes ?: emptyMap()
}

private fun com.grab.sizer.config.InfluxDbConfig.toSizerConfig(): InfluxDBConfig = InfluxDBConfig(
    dbName = dbName,
    url = url,
    username = username,
    password = password,
    reportTableName = reportTableName,
    databaseRetentionPolicy = retentionPolicy?.toSizerConfig() ?: DatabaseRetentionPolicy.createDefault()
)


private fun com.grab.sizer.config.RetentionPolicy.toSizerConfig(): DatabaseRetentionPolicy = DatabaseRetentionPolicy(
    name = name,
    duration = duration,
    shardDuration = shardDuration,
    replicationFactor = replicationFactor,
    isDefault = isDefault
)

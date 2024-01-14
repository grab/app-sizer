package com.grab.plugin.sizer.utils

import com.grab.plugin.sizer.AppSizePluginExtension
import com.grab.sizer.report.db.InfluxDbConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class PluginOutputProvider(
    private val extension: AppSizePluginExtension,
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDbConfig? {
        val influxDbPublisher = extension.metrics.influxDbPublisher
        if (influxDbPublisher.url.isPresent) {
            return InfluxDbConfig(
                dbName = influxDbPublisher.dbName.get(),
                url = influxDbPublisher.url.get(),
                username = influxDbPublisher.username.orNull,
                password = influxDbPublisher.password.orNull
            )
        }
        return null
    }

    override fun provideOutPutDirectory(): File = extension.metrics.local.outputDirectory.asFile.get()
}
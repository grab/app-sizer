package com.grab.plugin.sizer.utils

import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.db.InfluxDBConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class PluginOutputProvider(
    private val influxDBConfig: InfluxDBConfig?,
    private val customProperties: CustomProperties,
    private val outputFolder: File,
    private val projectInfo: ProjectInfo,
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDBConfig? = influxDBConfig
    override fun provideOutPutDirectory(): File = outputFolder
    override fun provideCustomProperties(): CustomProperties = customProperties
    override fun provideProjectInfo(): ProjectInfo = projectInfo
}

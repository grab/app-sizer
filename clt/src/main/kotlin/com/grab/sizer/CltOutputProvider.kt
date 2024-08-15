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

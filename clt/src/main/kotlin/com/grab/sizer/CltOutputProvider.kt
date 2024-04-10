package com.grab.sizer

import com.grab.sizer.report.db.InfluxDBConfig
import com.grab.sizer.utils.OutputProvider
import java.io.File

class CltOutputProvider(
    private val outputDirectory: File,
) : OutputProvider {
    override fun provideInfluxDbConfig(): InfluxDBConfig? {
        TODO("Not yet implemented")
    }

    override fun provideOutPutDirectory(): File = outputDirectory
}
package com.grab.sizer.report.db

import com.grab.sizer.utils.OutputProvider
import javax.inject.Inject

class DbReportDaoFactory @Inject constructor(private val outputProvider: OutputProvider) {
    fun create(): Set<ReportDao> = outputProvider.provideInfluxDbConfig()?.run {
        setOf(
            InfluxDbReportDao(
                InfluxDBFactory().create(this),
                this
            )
        )
    } ?: emptySet()
}
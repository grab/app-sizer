package com.grab.sizer.report.db

import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.OutputProvider
import org.influxdb.InfluxDB
import org.influxdb.InfluxDBIOException
import java.net.ConnectException
import javax.inject.Inject

class DbReportDaoFactory @Inject constructor(
    private val outputProvider: OutputProvider,
    private val logger: Logger
) {
    fun create(): Set<ReportDao> = outputProvider.provideInfluxDbConfig()?.run {
        val influxClient = InfluxDBFactory().create(this)
        setOf(InfluxDbReportDao(influxClient, this))
    } ?: emptySet()
}
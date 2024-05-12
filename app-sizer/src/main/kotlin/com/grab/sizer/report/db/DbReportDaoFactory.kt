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
        if (!influxClient.canConnectToSever()) return@run null
        setOf(InfluxDbReportDao(influxClient, this))
    } ?: emptySet()

    private fun InfluxDB.canConnectToSever(): Boolean = try {
        ping()
        true
    }catch (e: InfluxDBIOException) {
        logger.log("Can not connect to the InfluxDb database", e)
        e.printStackTrace()
        false
    } catch (e : ConnectException){
        logger.log("Can not connect to the InfluxDb database", e)
        e.printStackTrace()
        false
    }
}
package com.grab.tools.analyzer.report

import com.grab.pax.plugins.report.Field
import com.grab.pax.plugins.report.Metrics
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.pax.plugins.report.Tag

private const val METRICS_NAME = "mobile.pax.app.size.contributors"

class MetricsReportWriter(
    private val metricsPublisher: MetricsPublisher
) : ReportWriter {
    override fun write(appInfo: AppInfo, report: List<ReportItem>) {
        val metrics = report.map { item ->
            Metrics(
                name = METRICS_NAME,
                datadogName = METRICS_NAME,
                fields = buildFields(item),
                tags = buildTags(appInfo),
                timestamp = System.currentTimeMillis()
            )
        }

        metricsPublisher.publish(metrics)
    }

    private fun buildFields(report: ReportItem): List<Field> {
        return listOf(
            Field(
                name = "contributor_id",
                value = report.id.lowercase(),
                valueType = "string"
            ),
            Field(
                name = "contributor_size",
                value = report.totalDownloadSize.toString(),
                valueType = "integer"
            )
        )
    }

    private fun buildTags(appInfo: AppInfo): List<Tag> {
        return listOf(
            Tag(
                name = "project",
                value = "pax-android",
                valueType = "string"
            ),
            Tag(
                name = "app_version",
                value = appInfo.versionName,
                valueType = "string"
            ),
            Tag(
                name = "build_type",
                value = "production",
                valueType = "string"
            ),
            Tag(
                name = "device_name",
                value = appInfo.deviceName,
                valueType = "string"
            )
        )
    }
}
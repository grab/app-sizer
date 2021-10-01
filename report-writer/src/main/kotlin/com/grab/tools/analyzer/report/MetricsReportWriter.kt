package com.grab.tools.analyzer.report

import com.grab.pax.plugins.report.Field
import com.grab.pax.plugins.report.Metrics
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.pax.plugins.report.Tag

private const val METRICS_NAME = "mobile.pax.app.size.breakdown"

class MetricsReportWriter(
    private val metricsPublisher: MetricsPublisher,
    private val pipelineId: String,
) : ReportWriter {
    override fun write(appInfo: AppInfo, report: List<ReportItem>) {
        val metrics = report.map { item ->
            Metrics(
                name = METRICS_NAME,
                datadogName = METRICS_NAME,
                fields = buildFields(item),
                tags = buildTags(appInfo, item),
                timestamp = System.currentTimeMillis()
            )
        }
        metricsPublisher.publish(metrics)
    }

    private fun buildFields(report: ReportItem): List<Field> {
        return listOf(
            Field(
                name = report.id,
                value = report.totalDownloadSize.toString(),
                valueType = "integer"
            ),
            Field(
                name = "pipeline_id",
                value = pipelineId,
                valueType = "integer"
            )
        )
    }

    private fun buildTags(appInfo: AppInfo, report: ReportItem): List<Tag> {
        return listOf(
            Tag(
                name = "project",
                value = "pax-android",
                valueType = "string"
            ),
            Tag(
                name = "contributor",
                value = report.id.lowercase(),
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
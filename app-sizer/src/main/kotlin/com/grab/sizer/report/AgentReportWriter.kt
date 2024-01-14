package com.grab.sizer.report

import com.grab.pax.plugins.Metrics
import com.grab.pax.plugins.MetricsPublisher
import com.grab.pax.plugins.Field as MetricsField
import com.grab.pax.plugins.Tag as MetricsTag

class AgentReportWriter(
    private val metricsPublisher: MetricsPublisher
) : ReportWriter {
    override fun write(reportId: String, report: Report) {
        metricsPublisher.publish(
            reportId,
            report.rows.flatMap {
                it.toMetrics(report.projectInfo, report.customProperties, report.id)
            }
        )
    }

    private fun Row.toMetrics(
        projectInfo: ProjectInfo,
        customProperties: CustomProperties,
        metricsId: String
    ): List<Metrics> = listOf(
        Metrics(
            fields = fields.toMetricsFields() + customProperties.toCommonFields(),
            tags = fields.toMetricsTags() + projectInfo.toCommonTags(),
            timestamp = System.currentTimeMillis(),
            name = metricsId,
            datadogName = metricsId
        )
    )

    private fun List<Field>.toMetricsFields() = this.filterIsInstance<DefaultField>()
        .map { field ->
            MetricsField(
                name = field.name,
                value = field.value.toString(),
                valueType = field.toMetricsType()
            )
        }

    private fun List<Field>.toMetricsTags() = this.filterIsInstance<TagField>().map { field ->
        MetricsTag(
            name = field.name,
            value = field.value.toString(),
            valueType = field.toMetricsType()
        )
    }

    private fun CustomProperties.toCommonFields(): List<MetricsField> = map {
        MetricsField(
            name = it.key,
            value = it.value,
            valueType = "string"
        )
    }


    private fun ProjectInfo.toCommonTags(): List<MetricsTag> =
        listOf(
            MetricsTag(
                name = "project",
                value = projectName,
                valueType = "string"
            ),
            MetricsTag(
                name = "app_version",
                value = versionName,
                valueType = "string"
            ),
            MetricsTag(
                name = "build_type",
                value = buildType,
                valueType = "string"
            ),
            MetricsTag(
                name = "device_name",
                value = deviceName,
                valueType = "string"
            )
        )
}

private fun Field.toMetricsType(): String = when (value) {
    is Int -> "integer"
    is Long -> "integer"
    else -> "string"
}

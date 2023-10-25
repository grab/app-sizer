package com.grab.tools.analyzer.report

import com.grab.pax.plugins.report.Metrics
import com.grab.pax.plugins.report.MetricsPublisher
import com.grab.pax.plugins.report.Field as MetricsField
import com.grab.pax.plugins.report.Tag as MetricsTag

class AgentReportWriter(
    private val metricsPublisher: MetricsPublisher
) : ReportWriter {
    override fun write(reportId : String, report: Report) {
        metricsPublisher.publish(
            reportId,
            report.rows.flatMap {
                it.toMetrics(report.projectInfo, report.id)
            }
        )
    }

    private fun Row.toMetrics(projectInfo: ProjectInfo, metricsId: String): List<Metrics> = listOf(
        Metrics(
            fields = fields.toMetricsFields() + projectInfo.toCommonFields(),
            tags = fields.toMetricsTags() + projectInfo.toCommonTags(),
            timestamp = System.currentTimeMillis(),
            name = metricsId,
            datadogName = metricsId
        )
    )

    private fun List<Field>.toMetricsFields() = this.flatMap { field ->
        mutableListOf<MetricsField>().apply {
            if (field is HybridField) {
                add(
                    MetricsField(
                        name = "size",
                        value = field.value.toString(),
                        valueType = field.toMetricsType()
                    )
                )
            }
            if (field is DefaultField) {
                add(
                    MetricsField(
                        name = field.name,
                        value = field.value.toString(),
                        valueType = field.toMetricsType()
                    )
                )
            }
        }
    }

    private fun List<Field>.toMetricsTags() = this.flatMap { field ->
        mutableListOf<MetricsTag>().apply {
            when (field) {
                is HybridField -> {
                    add(
                        MetricsTag(
                            name = "contributor",
                            value = field.name,
                            valueType = "string"
                        )
                    )
                }
                is TagField -> {
                    add(
                        MetricsTag(
                            name = field.name,
                            value = field.value.toString(),
                            valueType = field.toMetricsType()
                        )
                    )
                }
            }

        }
    }


    private fun ProjectInfo.toCommonFields(): List<MetricsField> {
        return mutableListOf(
            MetricsField(
                name = "pipeline_id",
                value = pipelineId ?: "NA",
                valueType = "integer"
            )
        ).apply {
            if (tag != null) {
                add(
                    MetricsField(
                        name = "tag",
                        value = tag,
                        valueType = "string"
                    )
                )
            }
        }
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

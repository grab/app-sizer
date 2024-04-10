package com.grab.sizer.report.json

import com.google.gson.Gson
import com.grab.sizer.report.*
import java.io.File
import java.io.FileWriter

class JsonReportWriter(
    private val outputDirectory: File,
    private val gson: Gson = Gson()
) : ReportWriter {
    override fun write(reportId: String, report: Report){
        File(File(outputDirectory, report.projectInfo.deviceName), "$reportId-metrics.json").apply {
            initOutPutFile()
            FileWriter(this).use { fileWriter ->
                gson.toJson(
                    report.rows.flatMap { row ->
                        row.toMetrics(report.projectInfo, report.customProperties, report.id)
                    },
                    fileWriter
                )
            }
        }
    }


    private fun File.initOutPutFile() {
        if (!exists()) {
            if (!parentFile.exists())
                parentFile.mkdirs()
            createNewFile()
        }
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

    private fun List<com.grab.sizer.report.Field>.toMetricsFields() = this.filterIsInstance<DefaultField>()
        .map { field ->
            Field(
                name = field.name,
                value = field.value.toString(),
                valueType = field.toMetricsType()
            )
        }

    private fun List<com.grab.sizer.report.Field>.toMetricsTags() = this.filterIsInstance<TagField>().map { field ->
        Tag(
            name = field.name,
            value = field.value.toString(),
            valueType = field.toMetricsType()
        )
    }

    private fun CustomProperties.toCommonFields(): List<Field> = map {
        Field(
            name = it.key,
            value = it.value,
            valueType = "string"
        )
    }


    private fun ProjectInfo.toCommonTags(): List<Tag> =
        listOf(
            Tag(
                name = "project",
                value = projectName,
                valueType = "string"
            ),
            Tag(
                name = "app_version",
                value = versionName,
                valueType = "string"
            ),
            Tag(
                name = "build_type",
                value = buildType,
                valueType = "string"
            ),
            Tag(
                name = "device_name",
                value = deviceName,
                valueType = "string"
            )
        )
}

private fun com.grab.sizer.report.Field.toMetricsType(): String = when (value) {
    is Int -> "integer"
    is Long -> "integer"
    else -> "string"
}

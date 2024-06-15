package com.grab.sizer.report.json

import com.google.gson.Gson
import com.grab.sizer.report.*
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Named

typealias ReportField = com.grab.sizer.report.Field

class JsonReportWriter @Inject constructor(
    @Named(NAMED_OUTPUT_DIR) private val outputDirectory: File,
    private val projectInfo: ProjectInfo,
    private val customProperties: CustomProperties,
    private val gson: Gson = Gson()
) : ReportWriter {
    override fun write(report: Report) {
        File(File(outputDirectory, projectInfo.deviceName), "${report.id}-metrics.json").apply {
            initOutPutFile()
            FileWriter(this).use { fileWriter ->
                gson.toJson(
                    report.rows.flatMap { row ->
                        row.toMetrics(projectInfo, customProperties, report.id)
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

    private fun List<ReportField>.toMetricsFields() = this.filterIsInstance<DefaultField>()
        .map { field ->
            Field(
                name = field.name,
                value = field.value.toString(),
                valueType = field.toMetricsType()
            )
        }

    private fun List<ReportField>.toMetricsTags() = this.filterIsInstance<TagField>().map { field ->
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

private fun ReportField.toMetricsType(): String = when (value) {
    is Int -> "integer"
    is Long -> "integer"
    else -> "string"
}

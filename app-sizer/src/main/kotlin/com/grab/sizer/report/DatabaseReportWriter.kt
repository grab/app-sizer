package com.grab.sizer.report

import com.grab.sizer.report.db.ReportDao
import dagger.Lazy
import javax.inject.Inject

class DatabaseReportWriter @Inject constructor(
    private val reportDaoSet: Lazy<Set<ReportDao>>,
    private val projectInfo: ProjectInfo,
    private val customProperties: CustomProperties
) : ReportWriter {
    override fun write(report: Report) {
        /**
         * Add fields from [projectInfo] and [customProperties] to the report before write to the database
         */
        val addedCommonValueReport = report.copy(
            rows = report.rows.map { row ->
                row.copy(
                    fields = row.fields + customProperties.toTags() + projectInfo.toTags() + report.typeField()
                )
            }
        )
        reportDaoSet.get().forEach {
            it.addReport(addedCommonValueReport)
        }
    }

    private fun CustomProperties.toTags(): List<DefaultField> = map { property ->
        DefaultField(property.key, property.value)
    }

    private fun Report.typeField() = TagField("type", id)

    private fun ProjectInfo.toTags(): List<TagField> =
        listOf(
            TagField(
                name = "project",
                value = projectName,
            ),
            TagField(
                name = "app_version",
                value = versionName,
            ),
            TagField(
                name = "build_type",
                value = buildType,
            ),
            TagField(
                name = "device_name",
                value = deviceName,
            )
        )
}
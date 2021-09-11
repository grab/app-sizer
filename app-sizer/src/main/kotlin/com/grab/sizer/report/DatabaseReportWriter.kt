/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

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
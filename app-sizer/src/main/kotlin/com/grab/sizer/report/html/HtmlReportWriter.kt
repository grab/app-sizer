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

package com.grab.sizer.report.html

import com.google.gson.Gson
import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.FIELD_KEY_CONTRIBUTOR
import com.grab.sizer.report.FIELD_KEY_OWNER
import com.grab.sizer.report.FIELD_KEY_SIZE
import com.grab.sizer.report.FIELD_KEY_TAG
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.Report
import com.grab.sizer.report.ReportWriter
import com.grab.sizer.report.Row
import java.io.File

private const val TEMPLATE_RESOURCE = "/com/grab/sizer/report/html/dashboard-template.html"
private const val DATA_PLACEHOLDER = "__DASHBOARD_DATA__"
internal const val DASHBOARD_FILE_NAME = "index.html"

/**
 * Writes all reports of an analysis run into a single self-contained `index.html`
 * dashboard, placed next to the markdown and JSON reports of the device.
 *
 * The dashboard embeds the report data as JSON inside an HTML template; charts are
 * rendered client side with inline SVG. The file has no external dependencies (no CDN
 * scripts, fonts, or network calls), so it can be opened offline or from a CI artifact
 * browser.
 *
 * [write] is called once per report; the dashboard is regenerated on every call with
 * all reports accumulated so far, keeping the [ReportWriter] contract unchanged. The
 * last write therefore produces the complete dashboard.
 */
class HtmlReportWriter(
    private val outputDirectory: File,
    private val projectInfo: ProjectInfo,
    private val customProperties: CustomProperties,
    private val gson: Gson = Gson(),
) : ReportWriter {
    private val reports = LinkedHashMap<String, Report>()

    private val template: String by lazy {
        javaClass.getResourceAsStream(TEMPLATE_RESOURCE)
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: throw IllegalStateException("Dashboard template not found: $TEMPLATE_RESOURCE")
    }

    override fun write(report: Report) {
        reports[report.id] = report
        val dashboard = File(File(outputDirectory, projectInfo.deviceName), DASHBOARD_FILE_NAME)
        dashboard.parentFile?.mkdirs()
        dashboard.writeText(render())
    }

    private fun render(): String =
        template.replace(DATA_PLACEHOLDER, gson.toJson(toDashboardData()))

    private fun toDashboardData(): Map<String, Any> = mapOf(
        "project" to mapOf(
            "name" to projectInfo.projectName,
            "version" to projectInfo.versionName,
            "variant" to projectInfo.buildType,
            "device" to projectInfo.deviceName,
            "generatedAt" to System.currentTimeMillis(),
            "customProperties" to customProperties,
        ),
        "reports" to reports.mapValues { (_, report) -> report.rows.map { it.toEntry() } },
    )

    private fun Row.toEntry(): Map<String, Any> = buildMap {
        put("name", stringField(FIELD_KEY_CONTRIBUTOR) ?: name)
        put("size", (fields.find { it.name == FIELD_KEY_SIZE }?.value as? Number)?.toLong() ?: 0L)
        stringField(FIELD_KEY_OWNER)?.let { put("owner", it) }
        stringField(FIELD_KEY_TAG)?.let { put("tag", it) }
    }

    private fun Row.stringField(fieldName: String): String? =
        fields.find { it.name == fieldName }?.value?.toString()
}

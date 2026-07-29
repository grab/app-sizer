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

import com.grab.sizer.report.DefaultField
import com.grab.sizer.report.ProjectInfo
import com.grab.sizer.report.Report
import com.grab.sizer.report.Row
import com.grab.sizer.report.TagField
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HtmlReportWriterTest {

    @get:Rule
    val outputDir = TemporaryFolder()

    private val projectInfo = ProjectInfo(
        versionName = "1.2.3",
        projectName = "sample",
        deviceName = "device-1",
        buildType = "proRelease",
    )

    private lateinit var writer: HtmlReportWriter

    @Before
    fun setup() {
        writer = HtmlReportWriter(
            outputDirectory = outputDir.root,
            projectInfo = projectInfo,
            customProperties = mapOf("pipeline_id" to "42"),
        )
    }

    private fun row(name: String, size: Long, owner: String? = null): Row = Row(
        name = name,
        fields = listOfNotNull(
            TagField("contributor", name),
            DefaultField("size", size),
            owner?.let { TagField("owner", it) },
        ),
    )

    private fun dashboardFile(): File = File(File(outputDir.root, "device-1"), DASHBOARD_FILE_NAME)

    @Test
    fun `writes a self-contained dashboard next to the other reports`() {
        writer.write(Report(id = "apk_basic", name = "basic", rows = listOf(row("apk", 1048576))))

        val content = dashboardFile().readText()
        assertTrue(content.startsWith("<!DOCTYPE html>"))
        assertTrue(content.contains("\"apk_basic\""))
        assertFalse("template placeholder must be replaced", content.contains("__DASHBOARD_DATA__"))
        assertFalse("must not reference external resources", content.contains("https://cdn"))
    }

    @Test
    fun `accumulates reports across write calls`() {
        writer.write(Report(id = "apk_basic", name = "basic", rows = listOf(row("apk", 100))))
        writer.write(Report(id = "team", name = "team", rows = listOf(row("total", 60, owner = "team-a"))))

        val content = dashboardFile().readText()
        assertTrue(content.contains("\"apk_basic\""))
        assertTrue(content.contains("\"team\""))
        assertTrue(content.contains("team-a"))
    }

    @Test
    fun `embeds project info and custom properties`() {
        writer.write(Report(id = "apk_basic", name = "basic", rows = listOf(row("apk", 100))))

        val content = dashboardFile().readText()
        assertTrue(content.contains("\"sample\""))
        assertTrue(content.contains("\"proRelease\""))
        assertTrue(content.contains("\"pipeline_id\""))
    }

    @Test
    fun `report data cannot break out of the embedding script tag`() {
        val hostile = "</script><script>alert(1)</script>"
        writer.write(Report(id = "module", name = "m", rows = listOf(row(hostile, 10, owner = "team<b>"))))

        val content = dashboardFile().readText()
        assertFalse("closing tags in data must be escaped", content.contains(hostile))
        assertTrue(content.contains("\\u003c/script"))
    }

    @Test
    fun `rows keep owner and fall back to zero size`() {
        writer.write(
            Report(
                id = "module",
                name = "m",
                rows = listOf(
                    Row(name = "no-size", fields = listOf(TagField("contributor", "no-size"))),
                    row("owned", 5, owner = "team-b"),
                ),
            )
        )

        val content = dashboardFile().readText()
        assertTrue(content.contains("\"no-size\""))
        assertTrue(content.contains("team-b"))
    }
}

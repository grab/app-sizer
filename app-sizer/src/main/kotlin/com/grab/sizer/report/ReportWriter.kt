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


internal const val FIELD_KEY_CONTRIBUTOR = "contributor"
internal const val FIELD_KEY_SIZE = "size"
internal const val FIELD_KEY_OWNER = "owner"
internal const val FIELD_KEY_TAG = "tag"


typealias CustomProperties = Map<String, String>

/**
 * A data class that encapsulates information about a project.
 * It includes information such as the version name, project name, device name, and build type.
 * All these attributes will be treated as tags in the database.
 *
 * @property versionName The version name of the application.
 * @property projectName The name of the project.
 * @property deviceName The name of the device where the application is analysis.
 * @property buildType The type of the build (defaults to "production").
 */
data class ProjectInfo(
    val versionName: String,
    val projectName: String,
    val deviceName: String,
    val buildType: String = "production"
)


data class Row(
    val name: String,
    val fields: List<Field>
)

interface Field {
    val name: String
    val value: Any

    companion object {
        fun createDefault(name: String, value: Any): Field =
            DefaultField(name, value)
    }
}

/**
 * Those of items which having a large set of values which is not suitable for tag in a database
 */
data class DefaultField(
    override val name: String,
    override val value: Any,
) : Field

/**
 * Those of items which having a small set of values which is suitable for tag in a database
 */
data class TagField(
    override val name: String,
    override val value: Any,
) : Field

/**
 *
 */
data class Report(
    val id: String,
    val name: String,
    val rows: List<Row>
)

/**
 * The ReportWriter is an abstraction layer for the reporting process. It's allowing for flexibility in the reporting logics
 * It could be implemented to send reports to database, markdown, json file, etc.
 *
 * This interface is utilized by the [com.grab.sizer.AppSizer] to report the output.
 * [com.grab.sizer.AppSizer] will consume a set of [ReportWriter] instances provided by the [ReportModule] Dagger module.
 *
 * Implement this interface to add a new reporting method, and add it to the [ReportModule]
 * The new implementation will then be automatically consumed by all [com.grab.sizer.analyzer.Analyzer].
 */
interface ReportWriter {
    fun write(report: Report)
}




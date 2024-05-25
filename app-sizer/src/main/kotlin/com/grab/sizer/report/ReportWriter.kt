package com.grab.sizer.report


const val KEY_CONTRIBUTOR = "contributor"
const val KEY_SIZE = "size"

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
    val projectInfo: ProjectInfo,
    val rows: List<Row>,
    val customProperties: CustomProperties
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
    fun write(reportId: String, report: Report)
}




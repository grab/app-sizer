package com.grab.tools.analyzer.report

typealias CustomProperties = Map<String, String>

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
    val tag: String

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
    override val tag: String = ""
) : Field


/**
 * A hybrid class, which will pass the value as a field, but name as a tag to the database
 */
data class HybridField(
    override val name: String,
    override val value: Any,
    override val tag: String = ""
) : Field

/**
 * Those of items which having a small set of values which is suitable for tag in a database
 */
data class TagField(
    override val name: String,
    override val value: Any,
    override val tag: String = ""
) : Field


data class Report(
    val id: String,
    val name: String,
    val projectInfo: ProjectInfo,
    val rows: List<Row>,
    val customProperties: CustomProperties
)

interface ReportWriter {
    fun write(reportId: String, report: Report)
}




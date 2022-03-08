package com.grab.tools.analyzer.report


data class ProjectInfo(
    val versionName: String,
    val projectName: String,
    val deviceName: String,
    val pipelineId: String,
    val buildType: String = "production",
    val tag: String = ""
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
 * A hybrid class, which will pass the value as a field, but name as a tag to the a database
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
    val projectInfo: ProjectInfo,
    val rows: List<Row>,
    val id: String,
    val name: String
)

interface ReportWriter {
    fun write(report: Report)
}




package com.grab.plugin.size

import com.grab.tools.AnalyticsOption
import org.gradle.api.Project

private const val DEVICE_NAME_PARAM = "deviceName"
private const val PIPELINE_ID_PARAM = "pipeline"
private const val OPTION_PARAM = "option"
private const val LIBRARY_NAME_PARAM = "library"
private const val DEVICE_SPEC_PARAM = "deviceSpec"

internal interface ProjectParams {
    fun deviceName(): String?
    fun pipelineId(): String?
    fun option(): AnalyticsOption
    fun libraryName(): String?
    fun deviceSpec(): String?
}

internal fun Project.params(): ProjectParams = DefaultProjectParams(this)

private class DefaultProjectParams(private val project: Project) : ProjectParams {
    override fun deviceName(): String? = (project.findProperty(DEVICE_NAME_PARAM) as String?)
    override fun pipelineId(): String? = (project.findProperty(PIPELINE_ID_PARAM) as String?)

    override fun option(): AnalyticsOption = AnalyticsOption.fromString(project.findProperty(OPTION_PARAM) as String?)
    override fun libraryName(): String? = (project.findProperty(LIBRARY_NAME_PARAM) as String?)
    override fun deviceSpec(): String? = project.findProperty(DEVICE_SPEC_PARAM) as String?
}
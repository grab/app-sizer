package com.grab.tools.report

import com.grab.tools.analyzer.report.ProjectInfo

class ProjectInfoFactory(
    private val projectName: String,
    private val deviceName: String,
    private val pipelineId: String,
    private val buildType: String = "production",
    private val tag: String = ""
) {
    fun create(versionName: String): ProjectInfo {
        return ProjectInfo(
            projectName = projectName,
            versionName = versionName,
            deviceName = deviceName,
            pipelineId = pipelineId,
            buildType = buildType,
            tag = tag
        )
    }
}
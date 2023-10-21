package com.grab.tools.utils

import com.grab.tools.analyzer.report.ProjectInfo
import com.grab.tools.report.ProjectInfoProvider

class ProjectInfoProviderImpl(
    private val projectName: String,
    private val deviceName: String,
    private val pipelineId: String,
    private val versionName: String,
    private val buildType: String = "production",
    private val tag: String = ""
) : ProjectInfoProvider {
    override fun get(): ProjectInfo {
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
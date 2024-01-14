package com.grab.sizer.utils

import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo

class ProjectInfoProviderImpl(
    private val projectName: String,
    private val deviceName: String,
    private val pipelineId: String,
    private val versionName: String,
    private val buildType: String = "production",
    private val tag: String = ""
) : ProjectInfoProvider {
    override fun getProjectInfo(): ProjectInfo {
        return ProjectInfo(
            projectName = projectName,
            versionName = versionName,
            deviceName = deviceName,
            buildType = buildType
        )
    }

    override fun getCustomProperties(): CustomProperties {
        return mapOf(
            "pipelineId" to pipelineId,
            "tag" to tag
        )
    }
}
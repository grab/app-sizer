package com.grab.sizer.utils

import com.grab.sizer.config.Config
import com.grab.sizer.analyzer.ProjectInfoProvider
import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo

class ProjectInfoProviderImpl(
    private val config: Config,
    private val deviceName: String
) : ProjectInfoProvider {
    override fun getProjectInfo(): ProjectInfo {
        return ProjectInfo(
            projectName = config.projectInput.projectName,
            versionName = config.projectInput.version,
            deviceName = deviceName
        )
    }

    override fun getCustomProperties(): CustomProperties = config.report.customAttributes ?: emptyMap()
}
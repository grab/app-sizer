package com.grab.tools.analyzer

import com.grab.tools.analyzer.report.CustomProperties
import com.grab.tools.analyzer.report.ProjectInfo

interface ProjectInfoProvider {
    fun getProjectInfo(): ProjectInfo
    fun getCustomProperties() : CustomProperties
}
package com.grab.sizer.analyzer

import com.grab.sizer.report.CustomProperties
import com.grab.sizer.report.ProjectInfo

/**
 *
 */
interface ProjectInfoProvider {
    fun getProjectInfo(): ProjectInfo
    fun getCustomProperties() : CustomProperties
}
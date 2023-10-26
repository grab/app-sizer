package com.grab.tools.analyzer

import com.grab.tools.analyzer.report.ProjectInfo

interface ProjectInfoProvider {
    fun get(): ProjectInfo
}
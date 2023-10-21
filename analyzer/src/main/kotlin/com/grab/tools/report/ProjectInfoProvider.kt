package com.grab.tools.report

import com.grab.tools.analyzer.report.ProjectInfo

interface ProjectInfoProvider {
    fun get(): ProjectInfo
}
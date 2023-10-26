package com.grab.tools.report

import com.grab.tools.model.Contributor
import com.grab.tools.apk.ApkFileInfo






interface AnalyticReport {
    fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>)
}


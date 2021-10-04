package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.apk.ApkFileInfo

internal const val KILO_BYTE = 1024L
internal const val MEGA_BYTE = 1024L * 1024L

internal const val LIBRARY_METRICS_ID = "mobile.pax.app.size.breakdown.lib"
internal const val GENERAL_METRICS_ID = "mobile.pax.app.size.breakdown.tfs"
internal const val APK_METRICS_ID = "mobile.pax.app.size.breakdown.components"

interface AnalyticReport {
    fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>)
}


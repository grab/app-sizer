package com.grab.tools.report

import com.grab.tools.model.Contributor
import com.grab.tools.apk.ApkFileInfo

internal const val LIBRARY_METRICS_ID = "mobile.pax.app.size.lib5"
internal const val METRICS_ID_FEATURES = "mobile.pax.app.size.tfs5"
internal const val METRICS_ID_APK = "mobile.pax.app.size.app5"
internal const val METRICS_ID_MODULES = "mobile.pax.app.size.mds5"
internal const val METRICS_ID_BASIC_APK = "mobile.pax.app.size.components5"

interface AnalyticReport {
    fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>)
}


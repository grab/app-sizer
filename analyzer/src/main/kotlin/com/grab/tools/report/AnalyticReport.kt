package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.apk.ApkFileInfo

internal const val KILO_BYTE = 1024L
internal const val MEGA_BYTE = 1024L * 1024L

interface AnalyticReport {
    fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>)
}


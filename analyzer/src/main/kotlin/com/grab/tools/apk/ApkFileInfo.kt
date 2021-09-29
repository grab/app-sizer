package com.grab.tools.apk

import com.grab.tools.FileInfo
import com.grab.tools.RawFileInfo

data class ApkFileInfo(
    val name: String,
    val size: Long,
    val downloadSize: Long,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<FileInfo>,
    val dexes: Set<DexFileInfo>
) {
    val sumComponentsDownloadSize: Long
        get() = resources.sumOf { it.downloadSize } +
                nativeLibs.sumOf { it.downloadSize } +
                assets.sumOf { it.downloadSize } +
                dexes.sumOf { it.downloadSize } +
                others.sumOf { it.downloadSize }

}
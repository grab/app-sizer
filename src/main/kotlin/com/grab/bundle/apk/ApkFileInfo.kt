package com.grab.bundle.apk

import com.grab.bundle.RawFileInfo

data class ApkFileInfo(
    val name: String,
    val diskSize: Long,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val dexes: Set<DexFileInfo>
) {
    val compressedSize: Long
        get() = resources.sumOf { it.compressedSize } +
                nativeLibs.sumOf { it.compressedSize } +
                assets.sumOf { it.compressedSize } +
                dexes.sumOf { it.compressedSize } +
                others.sumOf { it.compressedSize }
}
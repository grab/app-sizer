package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

data class ApkFileInfo(
    val name: String,
    val size: Long,
    val downloadSize: Long,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<FileInfo>,
    val dexes: Set<DexFileInfo>,
    val manifestFileInfo : ManifestFileInfo
)
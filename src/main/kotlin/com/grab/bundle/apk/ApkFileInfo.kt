package com.grab.bundle.apk

import com.grab.bundle.RawFileInfo

data class ApkFileInfo_(
    private val resources: Set<RawFileInfo>,
    private val nativeLibs: Set<RawFileInfo>,
    private val assets: Set<RawFileInfo>,
    private val others: Set<RawFileInfo>,
    private val dexes: Set<DexFileInfo>
)
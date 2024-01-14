package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

data class JarFileInfo(
    val name: String,
    val path : String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
)
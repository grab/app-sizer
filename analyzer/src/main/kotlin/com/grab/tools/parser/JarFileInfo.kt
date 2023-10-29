package com.grab.tools.parser

import com.grab.tools.analyzer.model.ClassFileInfo
import com.grab.tools.analyzer.model.RawFileInfo

data class JarFileInfo(
    val name: String,
    val path : String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
)
package com.grab.tools.jar

import com.grab.tools.ClassFileInfo
import com.grab.tools.RawFileInfo

data class JarFileInfo(
    val name: String,
    val path : String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
)
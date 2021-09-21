package com.grab.bundle.jar

import com.grab.bundle.ClassFileInfo
import com.grab.bundle.RawFileInfo

data class JarFileInfo(
    val name: String,
    val path : String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
)
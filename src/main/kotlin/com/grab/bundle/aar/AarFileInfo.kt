package com.grab.bundle.aar

import com.grab.bundle.RawFileInfo
import com.grab.bundle.jar.JarFileInfo


class AarFileInfo(
    val name: String,
    val path: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val jars: Set<JarFileInfo>
)
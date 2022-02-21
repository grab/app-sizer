package com.grab.tools.aar

import com.grab.tools.model.RawFileInfo
import com.grab.tools.jar.JarFileInfo


class AarFileInfo(
    val name: String,
    val path: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val jars: Set<JarFileInfo>
)
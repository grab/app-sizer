package com.grab.tools.parser

import com.grab.tools.analyzer.model.RawFileInfo


class AarFileInfo(
    val name: String,
    val path: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val jars: Set<JarFileInfo>
)
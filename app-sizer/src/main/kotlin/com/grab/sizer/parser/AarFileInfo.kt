package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.RawFileInfo


data class AarFileInfo(
    val name: String,
    val path: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val jars: Set<JarFileInfo>
){
    override fun toString(): String = path
}
package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.RawFileInfo

interface BinaryFileInfo {
    val name: String
    val path: String

}

data class AarFileInfo(
    override val name: String,
    override val path: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<RawFileInfo>,
    val jars: Set<JarFileInfo>
) : BinaryFileInfo {
    override fun toString(): String = path
    override fun hashCode(): Int = path.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is AarFileInfo) return false
        return path == other.path
    }
}


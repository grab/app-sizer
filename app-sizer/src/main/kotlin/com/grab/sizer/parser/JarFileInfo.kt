package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

/**
 * A data class that represents a jar file parsed from the jar by [JarFileParser] or [JarStreamParser].
 * It contains details like the classes, path to the file, native libs and others
 */
data class JarFileInfo(
    override val name: String,
    override val path: String,
    val classes: Set<ClassFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val others: Set<RawFileInfo> = emptySet()
) : BinaryFileInfo {
    override fun hashCode(): Int = path.hashCode()
    override fun equals(other: Any?): Boolean {
        if (other !is JarFileInfo) return false
        return path == other.path
    }
}
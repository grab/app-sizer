package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

/**
 * A data class that represents a dex file parsed from the APK by [ApkFileParser].
 * It contains details like the dex file name, download size, set of class files, and normal size.
 *
 * @property name The name of the dex file.
 * @property downloadSize The download size of the dex file.
 * @property classes A set of ClassFileInfo objects representing classes contained in the dex file.
 * @property size The size of the dex file.
 */
data class DexFileInfo(
    val name: String,
    val downloadSize: Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    val size: Long,
) {
    // The total size of classes and other files in the dex file (computed lazily).
    val classSize: Long by lazy { classes.sumOf { it.size } + others.sumOf { it.size } }
}


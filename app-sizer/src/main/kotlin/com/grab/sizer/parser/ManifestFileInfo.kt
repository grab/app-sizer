package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.FileInfo
import java.io.File

data class ManifestFileInfo(
    val path: String,
    val versionCode: String? = null,
    val versionName: String? = null,
    override val downloadSize: Long,
    override val compressedSize: Long,
    override val size: Long
) : FileInfo {
    override val name: String
        get() = File(path).name
}

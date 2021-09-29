package com.grab.tools.apk

import com.grab.tools.FileInfo

data class ManifestFileInfo(
    val path: String,
    val versionCode: String? = null,
    val versionName: String? = null,
    override val downloadSize: Long,
    override val compressedSize: Long,
    override val size: Long
) : FileInfo

package com.grab.bundle.apk

import com.grab.bundle.ClassFileInfo
import com.grab.bundle.RawFileInfo

data class DexFileInfo(
    val name: String,
    val compressedSize: Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    val size : Long,
){
    val classSize : Long by lazy { classes.sumOf { it.size } + others.sumOf { it.size }}
}


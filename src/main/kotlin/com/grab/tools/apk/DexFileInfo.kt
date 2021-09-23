package com.grab.tools.apk

import com.grab.tools.ClassFileInfo
import com.grab.tools.RawFileInfo

data class DexFileInfo(
    val name: String,
    val downloadSize : Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    val size : Long,
){
    val classSize : Long by lazy { classes.sumOf { it.size } + others.sumOf { it.size }}
}


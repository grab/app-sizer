package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

data class DexFileInfo(
    val name: String,
    val downloadSize : Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    val size : Long,
){
    val classSize : Long by lazy { classes.sumOf { it.size } + others.sumOf { it.size }}
}


package com.grab.tools.parser

import com.grab.tools.analyzer.model.ClassFileInfo
import com.grab.tools.analyzer.model.RawFileInfo

data class DexFileInfo(
    val name: String,
    val downloadSize : Long,
    val classes: Set<ClassFileInfo>,
    val others: Set<RawFileInfo> = emptySet(),
    val size : Long,
){
    val classSize : Long by lazy { classes.sumOf { it.size } + others.sumOf { it.size }}
}


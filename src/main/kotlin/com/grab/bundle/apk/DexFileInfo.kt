package com.grab.bundle.apk

data class DexFileInfo(
    val name: String,
    val size: Long,
    val classes: Set<ClassFileInfo>
)

data class ClassFileInfo(
    val name: String,
    val size: Long
)
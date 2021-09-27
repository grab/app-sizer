package com.grab.tools


data class ClassFileInfo(
    val name: String,
    override val size: Long,
    override val compressedSize: Long = 0,
    override val downloadSize: Long = 0,
) : FileInfo {
    override fun equals(other: Any?): Boolean {
        if (other is ClassFileInfo) return name == other.name
        return super.equals(other)
    }

    override fun hashCode(): Int = name.hashCode()
}
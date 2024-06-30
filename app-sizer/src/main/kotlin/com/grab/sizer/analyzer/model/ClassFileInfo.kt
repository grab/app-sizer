package com.grab.sizer.analyzer.model


/**
 * Represents a class file in the aar/jar/apk file
 *
 * @property name The name of the class.
 * @property size The original, uncompressed size of the class file.
 * @property downloadSize The size of the class file in the binary that is downloadable from Google Play.
 */
data class ClassFileInfo(
    override val name: String,
    override val size: Long,
    override val downloadSize: Long = 0,
) : FileInfo {
    override fun equals(other: Any?): Boolean {
        if (other is ClassFileInfo) return name == other.name
        return super.equals(other)
    }

    override fun hashCode(): Int = name.hashCode()
}
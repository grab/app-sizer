package com.grab.sizer.analyzer.model

import java.io.File


/**
 * Specifies the type of file extracted from a jar or aar file.
 */
enum class FileType {
    RESOURCE, NATIVE_LIB, ASSET, DEX, JAR, OTHERS, CLASS
}

/**
 * Represents a file/class within a jar, aar or apk file and provides its various sizes.
 *
 * @property name The name of the file/class.
 * @property compressedSize The size of the file when compressed in the aar/jar file.
 * @property downloadSize The size of the file in the binary that is downloadable from Google Play.(zipped APKs)
 * @property size The original size of the file.
 */
interface FileInfo {
    val name: String
    val compressedSize: Long
    val downloadSize: Long
    val size: Long
}

/**
 * Defines a raw file which is not a class in a jar, aar or apk file
 *
 * @property path             The path to the raw file within the aar/jar file.
 * @property downloadSize     The size of the file in the downloadable binary from Google Play.
 * @property compressedSize   The size of the file when compressed, typically in aar/jar format.
 * @property size             The original, uncompressed size of the file.
 */
data class RawFileInfo(
    val path: String,
    override val downloadSize: Long,
    override val compressedSize: Long,
    override val size: Long
) : FileInfo {
    val type: FileType
        get() = when {
            path.startsWith("/res/") -> FileType.RESOURCE
            path.endsWith(".so", true) -> FileType.NATIVE_LIB
            path.startsWith("/assets/") -> FileType.ASSET
            path.endsWith(".dex") -> FileType.DEX
            path.endsWith(".jar") -> FileType.JAR
            path.endsWith(".class") -> FileType.CLASS
            else -> FileType.OTHERS
        }
    override val name: String
        get() = File(path).name

    override fun equals(other: Any?): Boolean {
        if (other is RawFileInfo) return path == other.path
        return super.equals(other)
    }

    override fun hashCode(): Int = path.hashCode()
}

internal fun Set<FileInfo>.castToClass(): Set<ClassFileInfo> = filterIsInstance<ClassFileInfo>().toSet()
internal fun Set<FileInfo>.castToRawFile(): Set<RawFileInfo> = filterIsInstance<RawFileInfo>().toSet()
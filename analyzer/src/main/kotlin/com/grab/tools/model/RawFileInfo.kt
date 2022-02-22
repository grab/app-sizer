package com.grab.tools.model

enum class FileType {
    RESOURCE, NATIVE_LIB, ASSET, DEX, JAR, OTHERS, CLASS, MANIFEST
}

interface FileInfo {
    val compressedSize: Long
    val downloadSize: Long
    val size: Long
}

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
            path.endsWith("AndroidManifest.xml") -> FileType.MANIFEST
            else -> FileType.OTHERS
        }

    override fun equals(other: Any?): Boolean {
        if (other is RawFileInfo) return path == other.path
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}

internal fun Set<FileInfo>.castToClass(): Set<ClassFileInfo> = filterIsInstance<ClassFileInfo>().toSet()
internal fun Set<FileInfo>.castToRawFile(): Set<RawFileInfo> = filterIsInstance<RawFileInfo>().toSet()
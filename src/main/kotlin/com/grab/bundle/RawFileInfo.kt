package com.grab.bundle

enum class FileType {
    RESOURCE, NATIVE_LIB, ASSET, OTHERS
}

data class RawFileInfo(
    val path: String,
    val size: Long
) {
    val type: FileType
        get() = when {
            path.startsWith("res") -> FileType.RESOURCE
            path.endsWith(".so", true) -> FileType.NATIVE_LIB
            path.startsWith("assets") -> FileType.ASSET
            else -> FileType.OTHERS
        }

    companion object {
        fun toType(type: String): FileType {
            return when (type) {
                "res" -> FileType.RESOURCE
                "lib" -> FileType.NATIVE_LIB
                else -> FileType.ASSET
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if(other is RawFileInfo) return path == other.path
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return path.hashCode()
    }
}
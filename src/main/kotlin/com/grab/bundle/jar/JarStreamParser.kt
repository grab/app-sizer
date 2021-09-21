package com.grab.bundle.jar

import com.grab.bundle.ClassFileInfo
import com.grab.bundle.FileType
import com.grab.bundle.RawFileInfo
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

interface JarStreamParser {
    fun parse(entry: ZipEntry, inputStream: InputStream): JarFileInfo
}

class JarStreamParserImpl : JarStreamParser {
    override fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo {
        ZipInputStream(inputStream).use { entries ->
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            var entry = entries.nextEntry
            while (entry != null) {
                val fileInfo = RawFileInfo(entry.name, entry.compressedSize, entry.size)
                when (fileInfo.type) {
                    FileType.CLASS -> classes.add(entry.toClass())
                    FileType.OTHERS -> others.add(fileInfo)
                }
                entry = entries.nextEntry
            }
            return JarFileInfo(
                name = jarEntry.name,
                path = "",
                others = others,
                nativeLibs = emptySet(),
                classes = classes
            )
        }
    }
}

internal fun ZipEntry.toClass(): ClassFileInfo {
    return ClassFileInfo(
        name = name.replace('/', '.').removeSuffix(".class"),
        compressedSize = compressedSize,
        size = size
    )
}



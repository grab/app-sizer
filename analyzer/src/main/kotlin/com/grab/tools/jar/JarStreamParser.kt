package com.grab.tools.jar

import com.grab.tools.model.ClassFileInfo
import com.grab.tools.model.FileType
import com.grab.tools.model.RawFileInfo
import com.grab.tools.apk.getPath
import com.grab.tools.di.AppScope
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

interface JarStreamParser {
    fun parse(entry: ZipEntry, inputStream: InputStream): JarFileInfo
}

@AppScope
class DefaultJarStreamParser @Inject constructor() : JarStreamParser {
    override fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo {
        ZipInputStream(inputStream).use { entries ->
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            var entry = entries.nextEntry
            while (entry != null) {
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    compressedSize = entry.compressedSize,
                    size = entry.size,
                    downloadSize = -1
                )
                when (fileInfo.type) {
                    FileType.CLASS -> classes.add(entry.toClass())
                    FileType.OTHERS -> others.add(fileInfo)
                }
                entry = entries.nextEntry
            }
            return JarFileInfo(
                name = jarEntry.getPath(),
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



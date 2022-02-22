package com.grab.tools.jar

import com.grab.tools.model.ClassFileInfo
import com.grab.tools.model.FileType
import com.grab.tools.model.RawFileInfo
import com.grab.tools.apk.getPath
import com.grab.tools.di.AppScope
import com.grab.tools.utils.JarFileQuery
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

interface JarFileParser {
    fun parse(file: File): JarFileInfo
    fun parseJars(dir: File, jarFileQuery: JarFileQuery): Set<JarFileInfo>
}

@AppScope
class DefaultJarFileParser @Inject constructor() : JarFileParser {
    override fun parse(file: File): JarFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    compressedSize = entry.compressedSize,
                    size = entry.size,
                    downloadSize = -1
                )
                when (fileInfo.type) {
                    FileType.NATIVE_LIB -> {
                        val fileInfoCorrectName = fileInfo.copy(
                            path = fileInfo.path.replace("jni", "lib")
                        )
                        nativeLibs.add(fileInfoCorrectName)
                    }
                    FileType.CLASS -> classes.add(entry.toClass())
                    FileType.OTHERS -> others.add(fileInfo)
                }
            }
            return JarFileInfo(
                name = file.name,
                path = file.path,
                others = others,
                nativeLibs = nativeLibs,
                classes = classes
            )
        }
    }

    override fun parseJars(dir: File, jarFileQuery: JarFileQuery): Set<JarFileInfo> {
        return jarFileQuery.query(dir)
            .map { file -> parse(file) }
            .toSet()
    }
}
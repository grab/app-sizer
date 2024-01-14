package com.grab.sizer.parser

import com.grab.sizer.di.AppScope
import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

interface JarFileParser {
    fun parseJars(files: Sequence<File>): Set<JarFileInfo>
}

@AppScope
class DefaultJarFileParser @Inject constructor() : JarFileParser {
    private fun parse(file: File): JarFileInfo {
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
                    else -> others.add(fileInfo)
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

    override fun parseJars(files: Sequence<File>): Set<JarFileInfo> {
        return files.map { file -> parse(file) }
            .toSet()
    }
}
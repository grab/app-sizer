package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject



/**
 * JarFileParser interface provides a method to parse a sequence of JAR files into a set of [JarFileInfo].
 * Note: For native libraries (*.so), their paths will be adjusted to ensure the files reside under the "lib" folder.
 * This modification facilitates mapping to native libraries in the APK file.
 */
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
                    size = entry.size,
                    downloadSize = -1
                )
                when (fileInfo.type) {
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
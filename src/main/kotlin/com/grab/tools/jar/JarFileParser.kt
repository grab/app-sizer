package com.grab.tools.jar

import com.grab.tools.ClassFileInfo
import com.grab.tools.FileQuery
import com.grab.tools.FileType
import com.grab.tools.RawFileInfo
import com.grab.tools.apk.getPath
import java.io.File
import java.util.zip.ZipFile

interface JarFileParser {
    fun parse(file: File): JarFileInfo
    fun parseJars(dir: File): Set<JarFileInfo>
}

class JarFileParserImpl(private val fileQuery: FileQuery) : JarFileParser {
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

    override fun parseJars(dir: File): Set<JarFileInfo> {
        return fileQuery.query(dir, "jar")
            .map { file -> parse(file) }
            .toSet()
    }
}
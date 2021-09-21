package com.grab.bundle.jar

import com.grab.bundle.ClassFileInfo
import com.grab.bundle.FileQuery
import com.grab.bundle.FileType
import com.grab.bundle.RawFileInfo
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
                val fileInfo = RawFileInfo(entry.name, entry.compressedSize, entry.size)
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
        return fileQuery.searchFile(dir, "jar")
            .map { file -> parse(file) }
            .toSet()
    }
}
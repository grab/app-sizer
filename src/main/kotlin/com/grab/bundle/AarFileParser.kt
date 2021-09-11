package com.grab.bundle

import java.io.File
import java.util.zip.ZipFile

internal typealias ApkFileInfo = Map<FileType, Set<RawFileInfo>>

class AarFileParser(private val fileQuery: FileQuery) {
    fun parse(file: File): Pair<String, ApkFileInfo> {
        return file.path to mutableMapOf<FileType, MutableSet<RawFileInfo>>().apply {
            ZipFile(file).use { zipFile ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    var fileInfo = RawFileInfo(entry.name, entry.compressedSize)
                    if(fileInfo.type == FileType.NATIVE_LIB){
                        fileInfo = fileInfo.copy(path = fileInfo.path.replace("jni", "lib"))
                    }
                    putIfAbsent(fileInfo.type, mutableSetOf())
                    get(fileInfo.type)?.add(fileInfo)
                }
            }
        }
    }

    fun parseAars(dir: File): Map<String, ApkFileInfo> {
        return fileQuery.searchFile(dir, "aar", "jar")
            .map { file -> parse(file) }
            .map { mapOf(it.first to it.second) }
            .reduce { m1, m2 -> m1 + m2 }
    }
}



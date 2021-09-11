package com.grab.bundle.apk

import com.grab.bundle.ApkFileInfo
import com.grab.bundle.FileQuery
import com.grab.bundle.FileType
import com.grab.bundle.RawFileInfo
import java.io.File
import java.util.zip.ZipFile

class ApkFileParser(private val fileQuery: FileQuery) {
    fun parse(file: File) : ApkFileInfo {
        return mutableMapOf<FileType, MutableSet<RawFileInfo>>().apply {
            ZipFile(file).use { zipFile ->
                val entries = zipFile.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val fileInfo = RawFileInfo(entry.name, entry.compressedSize)
                    putIfAbsent(fileInfo.type, mutableSetOf())
                    get(fileInfo.type)?.add(fileInfo)
                }
            }
        }
    }

    fun parseApks(dir : File) : ApkFileInfo = fileQuery.searchFile(dir, "apk")
        .map { apkFile -> parse(apkFile) }
        .reduce { acc, map -> acc + map }
}
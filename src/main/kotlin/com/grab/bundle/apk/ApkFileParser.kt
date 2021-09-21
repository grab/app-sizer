package com.grab.bundle.apk

import com.grab.bundle.FileQuery
import com.grab.bundle.FileType
import com.grab.bundle.RawFileInfo
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.util.zip.ZipFile


interface ApkFileParser {
    fun parse(file: File, proguardMap: ProguardMap?): ApkFileInfo
    fun parseApks(dir: File, proguardMap: ProguardMap?): Set<ApkFileInfo>
}

class ApkFileParserImpl(
    private val fileQuery: FileQuery,
    private val dexFileParser: DexFileParser
) : ApkFileParser {
    override fun parse(file: File, proguardMap: ProguardMap?): ApkFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val dexes = mutableSetOf<DexFileInfo>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val fileInfo = RawFileInfo(entry.name, entry.compressedSize, entry.size)

                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.DEX -> dexes.add(dexFileParser.parse(entry, zipFile.getInputStream(entry), proguardMap))
                    else -> others.add(fileInfo)
                }
            }



            return ApkFileInfo(
                name = file.name,
                resources = resources,
                assets = assets,
                nativeLibs = nativeLibs,
                others = others,
                dexes = dexes,
                diskSize = file.length()
            )
        }
    }

    override fun parseApks(dir: File, proguardMap: ProguardMap?): Set<ApkFileInfo> =
        fileQuery.searchFile(dir, "apk")
            .map { apkFile -> parse(apkFile, proguardMap) }
            .toSet()
}
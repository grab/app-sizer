package com.grab.tools.apk

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.tools.utils.FileQuery
import com.grab.tools.FileType
import com.grab.tools.RawFileInfo
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile


interface ApkFileParser {
    fun parse(file: File, proguardMap: ProguardMap?): ApkFileInfo
    fun parseApks(dir: File, proguardMap: ProguardMap?): Set<ApkFileInfo>
}

class ApkFileParserImpl(
    private val fileQuery: FileQuery,
    private val dexFileParser: DexFileParser,
    private val apkSizeCalculator: ApkSizeCalculator,
) : ApkFileParser {
    override fun parse(file: File, proguardMap: ProguardMap?): ApkFileInfo {
        val apkSizeInfo = apkSizeCalculator.parseSize(file.toPath())
        return parseApkFile(file, apkSizeInfo, proguardMap)
    }

    private fun parseApkFile(file: File, apkSizeInfo: ApkSizeInfo, proguardMap: ProguardMap?): ApkFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            val dexes = mutableSetOf<DexFileInfo>()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val path = entry.getPath()
                val downloadSize = apkSizeInfo.downloadFileSizeMap[path] ?: 0
                val rawSize = apkSizeInfo.rawFileSizeMap[path] ?: 0

                val fileInfo = RawFileInfo(
                    path = path,
                    downloadSize = downloadSize,
                    compressedSize = entry.compressedSize,
                    size = rawSize
                )

                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.DEX -> dexes.add(dexFileParser.parse(entry, zipFile.getInputStream(entry), apkSizeInfo, proguardMap))
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
                size = apkSizeInfo.size,
                downloadSize = apkSizeInfo.downloadSize
            )
        }
    }

    override fun parseApks(dir: File, proguardMap: ProguardMap?): Set<ApkFileInfo> =
        fileQuery.query(dir, "apk")
            .map { apkFile -> parse(apkFile, proguardMap) }
            .toSet()

    private fun ApkSizeCalculator.parseSize(path: Path): ApkSizeInfo = ApkSizeInfo(
        downloadSize = apkSizeCalculator.getFullApkDownloadSize(path),
        size = apkSizeCalculator.getFullApkDownloadSize(path),
        downloadFileSizeMap = apkSizeCalculator.getDownloadSizePerFile(path),
        rawFileSizeMap = apkSizeCalculator.getRawSizePerFile(path)
    )
}

internal fun ZipEntry.getPath() = "/$name"

class ApkSizeInfo(
    val downloadSize: Long,
    val size: Long,
    val downloadFileSizeMap: Map<String, Long>,
    val rawFileSizeMap: Map<String, Long>
)
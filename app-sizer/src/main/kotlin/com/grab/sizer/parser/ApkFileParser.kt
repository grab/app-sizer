package com.grab.sizer.parser

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.sizer.di.AppScope
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject

interface ApkFileParser {
    fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo>
}

@AppScope
class DefaultApkFileParser @Inject constructor(
    private val dexFileParser: DexFileParser,
    private val apkSizeCalculator: ApkSizeCalculator,
    private val manifestFileParser: ManifestFileParser
) : ApkFileParser {
    private fun parse(file: File, proguardMap: ProguardMap): ApkFileInfo {
        val apkSizeInfo = apkSizeCalculator.parseSize(file.toPath())
        return parseApkFile(file, apkSizeInfo, proguardMap)
    }

    private fun parseApkFile(file: File, apkSizeInfo: ApkSizeInfo, proguardMap: ProguardMap): ApkFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<FileInfo>()
            val dexes = mutableSetOf<DexFileInfo>()
            var manifestFileInfo = ManifestFileInfo(downloadSize = 0, compressedSize = 0, size = 0, path = "")
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
                    FileType.DEX -> dexes.add(
                        dexFileParser.parse(
                            entry,
                            zipFile.getInputStream(entry),
                            apkSizeInfo,
                            proguardMap
                        )
                    )
                    FileType.MANIFEST -> {
                        manifestFileInfo = manifestFileParser.parse(zipFile.getInputStream(entry), fileInfo)
                        others.add(manifestFileInfo)
                    }
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
                downloadSize = apkSizeInfo.downloadSize,
                manifestFileInfo = manifestFileInfo
            )
        }
    }

    override fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo> = apks
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
package com.grab.sizer.parser

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import javax.inject.Inject


internal interface ApkFileParser {
    /**
     * Parses a sequence of APK files and use R8 mapping file to extract and return the set of APK file information.
     * This method de-obfuscates class names to make them readable, and estimates the download size of each file
     * in the [ApkFileInfo] output.
     *
     * @param apks A sequence of APK files to be parsed.
     * @param proguardMap A ProguardMap used for de-obfuscating class names in the APK files.
     * @return A set of ApkFileInfo instances, each representing information about a parsed APK file.
     */
    fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo>
}

@AppScope
internal class DefaultApkFileParser @Inject constructor(
    private val dexFileParser: DexFileParser,
    private val apkSizeCalculator: ApkSizeCalculator
) : ApkFileParser {
    override fun parseApks(apks: Sequence<File>, proguardMap: ProguardMap): Set<ApkFileInfo> = apks
        .map { apkFile -> parse(apkFile, proguardMap) }
        .toSet()

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

    private fun ApkSizeCalculator.parseSize(path: Path): ApkSizeInfo = ApkSizeInfo(
        downloadSize = getFullApkDownloadSize(path),
        size = getFullApkDownloadSize(path),
        downloadFileSizeMap = getDownloadSizePerFile(path),
        rawFileSizeMap = getRawSizePerFile(path)
    )
}

internal fun ZipEntry.getPath() = "/$name"

internal class ApkSizeInfo(
    val downloadSize: Long,
    val size: Long,
    val downloadFileSizeMap: Map<String, Long>,
    val rawFileSizeMap: Map<String, Long>
)
package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.RawFileInfo

/**
 * A data class that represents the output after parse an APK file by [ApkFileParser].
 * It encapsulates the parsed data from an APK file, including information such as its name,
 * size, download size, and the set of resources, native libraries, assets, others, and dex files it contains.
 *
 * @property name The name of the APK file.
 * @property resources A set of resources contained in the APK file.
 * @property nativeLibs A set of native libraries contained in the APK file.
 * @property assets A set of assets files contained in the APK file.
 * @property others A set of FileInfo objects representing other files in the APK file.
 * @property dexes A set of [DexFileInfo] objects representing dex files in the APK file.
 */
data class ApkFileInfo(
    val name: String,
    val resources: Set<RawFileInfo>,
    val nativeLibs: Set<RawFileInfo>,
    val assets: Set<RawFileInfo>,
    val others: Set<FileInfo>,
    val dexes: Set<DexFileInfo>
)
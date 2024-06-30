package com.grab.sizer.parser


import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.di.AppScope
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.log
import org.jf.dexlib2.dexbacked.DexBackedClassDef
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import javax.inject.Inject


/**
 * DexFileParser interface provides a method for parsing a dex file located within an APK file.
 */
internal interface DexFileParser {
    /**
     * Parses a dex file within the APK.
     * Reads input stream of the dex file, retrieves class details and converts the file attributes into a DexFileInfo object.
     */
    fun parse(
        entry: ZipEntry,
        inputStream: InputStream,
        apkSizeInfo: ApkSizeInfo,
        proguardMap: ProguardMap? = null
    ): DexFileInfo
}

/**
 * DefaultDexFileParser is the default implementation of the DexFileParser interface.
 * It utilizes the DexBackedDexFile class from the 'org.smali:dexlib2' library to perform dex file parsing.
 */
@AppScope
internal class DefaultDexFileParser @Inject constructor(
    private val logger: Logger
) : DexFileParser {
    override fun parse(
        entry: ZipEntry,
        inputStream: InputStream,
        apkSizeInfo: ApkSizeInfo,
        proguardMap: ProguardMap?
    ): DexFileInfo {
        val dexBackedDexFile = DexBackedDexFile.fromInputStream(null, BufferedInputStream(inputStream))

        val classes = dexBackedDexFile.classes
            .map { classDef -> fromDex(classDef, proguardMap) }
            .toSet()

        val path = entry.getPath()
        val dexDownloadSize = apkSizeInfo.downloadFileSizeMap[path] ?: 0
        val dexClassesSize = classes.sumOf { it.size }
        val ratio = dexDownloadSize.toDouble() / dexClassesSize
        return DexFileInfo(
            name = path,
            downloadSize = dexDownloadSize,
            size = apkSizeInfo.rawFileSizeMap[path] ?: 0,
            classes = classes.map { it.copy(downloadSize = (it.size * ratio).toLong()) }.toSet()
        )
    }

    private fun fromDex(classDef: DexBackedClassDef, proguardMap: ProguardMap?): ClassFileInfo {
        val className = classDef.type.removePrefix("L").replace('/', '.').removeSuffix(";")
        if (proguardMap != null && proguardMap.getClassName(className) == null) {
            logger.log("Can not find $className in from proguard mapping file")
        }
        return ClassFileInfo(
            name = proguardMap?.getClassName(className) ?: className,
            size = classDef.size.toLong()
        )
    }
}
package com.grab.tools.apk


import com.grab.tools.ClassFileInfo
import com.grab.tools.di.AppScope
import com.grab.tools.log.log
import org.jf.dexlib2.dexbacked.DexBackedClassDef
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.BufferedInputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import javax.inject.Inject

interface DexFileParser {
    fun parse(
        entry: ZipEntry,
        inputStream: InputStream,
        apkSizeInfo: ApkSizeInfo,
        proguardMap: ProguardMap? = null
    ): DexFileInfo
}

@AppScope
class DefaultDexFileParser @Inject constructor() : DexFileParser {
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
        return DexFileInfo(
            name = path,
            downloadSize = apkSizeInfo.downloadFileSizeMap[path] ?: 0,
            size = apkSizeInfo.rawFileSizeMap[path] ?: 0,
            classes = classes,
        )
    }

    private fun fromDex(classDef: DexBackedClassDef, proguardMap: ProguardMap?): ClassFileInfo {
        val className = classDef.type.removePrefix("L").replace('/', '.').removeSuffix(";")
        if (proguardMap != null && proguardMap.getClassName(className) == null) {
            log("Can not find $className in from proguard mapping file")
        }
        return ClassFileInfo(
            name = proguardMap?.getClassName(className) ?: className,
            size = classDef.size.toLong()
        )
    }
}
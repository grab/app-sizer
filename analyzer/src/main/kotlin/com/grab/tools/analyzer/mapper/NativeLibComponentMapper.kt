package com.grab.tools.analyzer.mapper

import com.grab.tools.analyzer.model.RawFileInfo
import com.grab.tools.parser.AarFileInfo
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.JarFileInfo
import javax.inject.Inject

internal class NativeLibComponentMapper @Inject constructor() : ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        val apkLibs = apks.flatMap { apk -> apk.nativeLibs }
        val libraryMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.nativeLibs.forEach { file ->
                    put(file, aar.path)
                }
            }
            jars.forEach { jar ->
                jar.nativeLibs.forEach { file ->
                    put(file, jar.path)
                }
            }
        }
        val noOwnerNativeLib = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkLibs.forEach { nativeLib ->
                val libName = libraryMap[nativeLib]
                if (libName != null) {
                    putIfAbsent(libName, mutableSetOf())
                    get(libName)?.add(nativeLib)
                }else{
                    noOwnerNativeLib.add(nativeLib)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerNativeLib
        )
    }
}
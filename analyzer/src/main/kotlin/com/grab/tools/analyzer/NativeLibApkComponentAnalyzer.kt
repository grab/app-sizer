package com.grab.tools.analyzer

import com.grab.tools.RawFileInfo
import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo
import javax.inject.Inject

class NativeLibApkComponentAnalyzer @Inject constructor() : ApkComponentAnalyzer {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentAnalyzerResult {
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
        return ComponentAnalyzerResult(
            contributors = contributors,
            noOwnerData = noOwnerNativeLib
        )
    }
}
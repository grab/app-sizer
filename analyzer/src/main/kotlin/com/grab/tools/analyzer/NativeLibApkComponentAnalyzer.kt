package com.grab.tools.analyzer

import com.grab.tools.RawFileInfo
import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo

class NativeLibApkComponentAnalyzer : ApkComponentAnalyzer {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): RawContributors {
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
        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkLibs.forEach { resource ->
                val libName = libraryMap[resource]
                if (libName != null) {
                    putIfAbsent(libName, mutableSetOf())
                    get(libName)?.add(resource)
                }
            }
        }
    }
}
package com.grab.bundle.analyzer

import com.grab.bundle.RawFileInfo
import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo
import com.grab.bundle.log.log

class NativeLibAnalyzer : Analyzer {
    companion object {
        const val TAG = "NativeLibAnalyzer"
    }

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
            apkLibs?.forEach { resource ->
                val libName = libraryMap[resource]
                if (libName != null) {
                    putIfAbsent(libName, mutableSetOf())
                    log("Lib name : $libName")
                    log("Native path : ${resource.path}")
                    get(libName)?.add(resource)
                }
            }
        }
    }
}
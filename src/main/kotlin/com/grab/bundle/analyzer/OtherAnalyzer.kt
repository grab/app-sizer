package com.grab.bundle.analyzer


import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo

class OtherAnalyzer : Analyzer {
    companion object {
        const val TAG = "OtherAnalyzer"
    }

    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): RawContributors {
//        val apkOthers = apks.flatMap { apk -> apk.others }
//
//        val aarsAssetMap = mutableMapOf<RawFileInfo, String>().apply {
//            aars.forEach { aar ->
//                aar.others.forEach { file ->
//                    put(file, aar.name)
//                }
//            }
//        }
//
//        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
//            apkAssets.forEach { asset ->
//                val aarName = aarsAssetMap[asset]
//                if (aarName != null) {
//                    putIfAbsent(aarName, mutableSetOf())
//                    get(aarName)?.add(asset)
//                }
//            }
//        }
        return emptyMap()
    }
}
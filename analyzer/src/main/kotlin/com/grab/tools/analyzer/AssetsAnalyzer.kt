package com.grab.tools.analyzer


import com.grab.tools.RawFileInfo
import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo

class AssetsAnalyzer : Analyzer {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars : Set<JarFileInfo>): RawContributors {
        val apkAssets = apks.flatMap { apk -> apk.assets }

        val aarsAssetMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.assets.forEach { file ->
                    put(file, aar.path)
                }
            }
        }

        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkAssets.forEach { asset ->
                val aarName = aarsAssetMap[asset]
                if (aarName != null) {
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(asset)
                }
            }
        }
    }
}
package com.grab.bundle.analyzer


import com.grab.bundle.RawFileInfo
import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo

class AssetsAnalyzer : Analyzer {
    companion object {
        const val TAG = "AssetsAnalyzer"
    }

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
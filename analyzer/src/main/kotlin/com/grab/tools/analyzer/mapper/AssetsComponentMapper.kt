package com.grab.tools.analyzer.mapper


import com.grab.tools.analyzer.model.RawFileInfo
import com.grab.tools.parser.AarFileInfo
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.JarFileInfo
import javax.inject.Inject

internal class AssetsComponentMapper @Inject constructor() : ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars : Set<JarFileInfo>): ComponentMapperResult {
        val apkAssets = apks.flatMap { apk -> apk.assets }

        val aarsAssetMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.assets.forEach { file ->
                    put(file, aar.path)
                }
            }
        }
        val noOwnerAssets = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkAssets.forEach { asset ->
                val aarName = aarsAssetMap[asset]
                if (aarName != null) {
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(asset)
                }else{
                    noOwnerAssets.add(asset)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerAssets
        )
    }
}
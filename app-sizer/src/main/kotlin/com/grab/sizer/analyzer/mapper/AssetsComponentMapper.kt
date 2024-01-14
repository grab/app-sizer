package com.grab.sizer.analyzer.mapper


import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
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
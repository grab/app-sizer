package com.grab.sizer.analyzer.mapper


import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on assets.
 */
internal class AssetComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        val apkAssets = flatMap { apk -> apk.assets }

        val aarsAssetMap = mutableMapOf<RawFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.assets.forEach { file ->
                    put(file, aar)
                }
            }
        }
        val noOwnerAssets = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<RawFileInfo>>().apply {
            apkAssets.forEach { asset ->
                val aar = aarsAssetMap[asset]
                if (aar != null) {
                    putIfAbsent(aar, mutableSetOf())
                    get(aar)?.add(asset)
                } else {
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
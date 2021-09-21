package com.grab.bundle.analyzer

import com.grab.bundle.RawFileInfo
import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo

class ResourceAnalyzer : Analyzer {
    companion object {
        const val TAG = "ResourceAnalyzer"
    }

    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): RawContributors {
        val apkResource = apks.flatMap { it.resources }
        val aarsResMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.resources.forEach { file -> put(file, aar.path) }
            }
        }
        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkResource.forEach { resource ->
                if (aarsResMap.containsKey(resource)) {
                    val aarName = aarsResMap[resource]!!
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(resource)
                }
            }
        }
    }
}
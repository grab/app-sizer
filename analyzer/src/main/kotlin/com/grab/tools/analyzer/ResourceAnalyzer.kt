package com.grab.tools.analyzer

import com.grab.tools.RawFileInfo
import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo

class ResourceAnalyzer : Analyzer {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): RawContributors {
        val apkResource = apks.flatMap { it.resources }
        val aarsToResMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.resources.forEach { file -> put(file, aar.path) }
            }
        }
        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkResource.forEach { resource ->
                val aarName = aarsToResMap[resource]
                if (aarName != null) {
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(resource)
                }
            }
        }
    }
}
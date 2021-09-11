package com.grab.bundle.analytic

import com.grab.bundle.ApkFileInfo
import com.grab.bundle.Contributors
import com.grab.bundle.RawFileInfo
import com.grab.bundle.FileType

class ResourceAnalytic : Analytic {
    companion object{
        const val TAG = "ResourceAnalytic"
    }
    override fun analytic(apkInfo: ApkFileInfo, aars: Map<String, ApkFileInfo>): Contributors {
        val apkResource = apkInfo[FileType.RESOURCE]
        val aarsResMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aarEntry ->
                aarEntry.value[FileType.RESOURCE]?.forEach { item ->
                    put(item, aarEntry.key)
                }
            }
        }
        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkResource?.forEach { resource ->
                if(aarsResMap.containsKey(resource)){
                    val aarName = aarsResMap[resource]!!
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(resource)
                }
            }
        }
    }
}
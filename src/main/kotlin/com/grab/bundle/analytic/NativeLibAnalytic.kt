package com.grab.bundle.analytic

import com.grab.bundle.ApkFileInfo
import com.grab.bundle.Contributors
import com.grab.bundle.RawFileInfo
import com.grab.bundle.FileType

class NativeLibAnalytic : Analytic {
    companion object{
        const val TAG = "NativeLibAnalytic"
    }
    override fun analytic(apkInfo: ApkFileInfo, aars: Map<String, ApkFileInfo>): Contributors {
        val apkLibs = apkInfo[FileType.NATIVE_LIB]
        val aarsLibMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aarEntry ->
                aarEntry.value[FileType.NATIVE_LIB]?.forEach { item ->
                    put(item, aarEntry.key)
                }
            }
        }
        return mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkLibs?.forEach { resource ->
                if(aarsLibMap.containsKey(resource)){
                    val aarName = aarsLibMap[resource]!!
                    putIfAbsent(aarName, mutableSetOf())
                    get(aarName)?.add(resource)
                }
            }
        }
    }
}
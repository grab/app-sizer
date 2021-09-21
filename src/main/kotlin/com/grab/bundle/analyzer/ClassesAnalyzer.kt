package com.grab.bundle.analyzer


import com.grab.bundle.ClassFileInfo
import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo

class ClassesAnalyzer : Analyzer {
    companion object {
        const val TAG = "DexFileAnalyzer"
    }

    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): RawContributors {
        val apkClasses = apks.flatMap { apk -> apk.dexes }.flatMap { dex -> dex.classes }
        val libClassMap = mutableMapOf<ClassFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.jars.forEach { jar ->
                    jar.classes.forEach { clazz ->
                        put(clazz, aar.path)
                    }
                }
            }
            jars.forEach { jar ->
                jar.classes.forEach { clazz ->
                    put(clazz, jar.path)
                }
            }
        }

        return mutableMapOf<String, MutableSet<ClassFileInfo>>().apply {
            apkClasses.forEach { clazz ->
                val libName = libClassMap[clazz]
                if (libName != null) {
                    putIfAbsent(libName, mutableSetOf())
                    get(libName)?.add(clazz)
                }
            }
        }
    }
}
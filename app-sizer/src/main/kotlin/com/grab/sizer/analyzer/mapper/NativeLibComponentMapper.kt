package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
import java.io.File
import javax.inject.Inject

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on native libraries.
 */
internal class NativeLibComponentMapper @Inject constructor() : ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        val apkLibs = apks.flatMap { apk -> apk.nativeLibs }
            .map { it.trimPath() }

        val libraryMap = mutableMapOf<RawFileInfo, String>().apply {
            aars.forEach { aar ->
                aar.nativeLibs.forEach { file ->
                    put(file.trimPath(), aar.path)
                }
            }
            jars.forEach { jar ->
                jar.nativeLibs.forEach { file ->
                    put(file.trimPath(), jar.path)
                }
            }
        }
        val noOwnerNativeLib = mutableSetOf<RawFileInfo>()
        val contributors = mutableMapOf<String, MutableSet<RawFileInfo>>().apply {
            apkLibs.forEach { nativeLib ->
                val libName = libraryMap[nativeLib]
                if (libName != null) {
                    putIfAbsent(libName, mutableSetOf())
                    get(libName)?.add(nativeLib)
                }else{
                    noOwnerNativeLib.add(nativeLib)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerNativeLib
        )
    }

    /**
     * There are different between APK and AAR native file path.
     * This method will remove the pre-fix path for the so file, to ensure the mapping working as expected
     * Example,
     * APK: /lib/armeabi-v7a/sample.so -> armeabi-v7a/sample.so
     * AAR: /jni/armeabi-v7a/sample.so -> armeabi-v7a/sample.so
     */
    private fun RawFileInfo.trimPath() : RawFileInfo{
        val file = File(path)
        val parent = File(path).parentFile.name
        return copy(
            path = "/$parent/${file.name}"
        )
    }
}
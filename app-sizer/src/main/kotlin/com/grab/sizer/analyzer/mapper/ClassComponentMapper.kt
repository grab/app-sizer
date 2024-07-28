package com.grab.sizer.analyzer.mapper


import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject

private const val AUTO_GENERATION_LAMBDA = "-\$\$Lambda\$"

/**
 * Analyzes, maps and creates a ComponentMapperResult focusing on classes.
 */
internal class ClassComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentMapperResult {
        val apkClasses = flatMap { apk -> apk.dexes }.flatMap { dex -> dex.classes }
        val libClassMap = mutableMapOf<ClassFileInfo, BinaryFileInfo>().apply {
            aars.forEach { aar ->
                aar.jars.forEach { jar ->
                    jar.classes.forEach { clazz ->
                        put(clazz, aar)
                    }
                }
            }
            jars.forEach { jar ->
                jar.classes.forEach { clazz ->
                    put(clazz, jar)
                }
            }
        }
        val noOwnerClasses = mutableSetOf<ClassFileInfo>()
        val contributors = mutableMapOf<BinaryFileInfo, MutableSet<ClassFileInfo>>().apply {
            apkClasses.forEach { clazz ->
                val lib = libClassMap[clazz] ?: libClassMap[clazz.tryOriginalClass()]
                if (lib != null) {
                    putIfAbsent(lib, mutableSetOf())
                    get(lib)?.add(clazz)
                } else {
                    noOwnerClasses.add(clazz)
                }
            }
        }
        return ComponentMapperResult(
            contributors = contributors,
            noOwnerData = noOwnerClasses
        )
    }

    private fun ClassFileInfo.tryOriginalClass(): ClassFileInfo {
        /**
         * Auto generated lambda.
         * Ex: androidx.core.widget.-$$Lambda$ContentLoadingProgressBar$aW9csiS0dCdsR2nrqov9CuXAmGo
         */
        if (name.contains(AUTO_GENERATION_LAMBDA)) {
            var newName = name.replace(AUTO_GENERATION_LAMBDA, "")
            if (newName.lastIndexOf("$") > 0)
                newName = newName.removeRange(newName.lastIndexOf("$"), newName.length)
            return copy(name = newName)
        }
        return this
    }
}
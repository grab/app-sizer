package com.grab.tools.analyzer.mapper


import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo
import javax.inject.Inject

class OtherComponentMapper @Inject constructor(): ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentAnalyzerResult {
        return ComponentAnalyzerResult(
            contributors = emptyMap(),
            noOwnerData = apks.flatMap { it.others }.toSet()
        )
    }
}
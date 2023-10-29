package com.grab.tools.analyzer.mapper


import com.grab.tools.parser.AarFileInfo
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.JarFileInfo
import javax.inject.Inject

internal class OtherComponentMapper @Inject constructor(): ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        return ComponentMapperResult(
            contributors = emptyMap(),
            noOwnerData = apks.flatMap { it.others }.toSet()
        )
    }
}
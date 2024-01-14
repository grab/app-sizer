package com.grab.sizer.analyzer.mapper


import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject

internal class OtherComponentMapper @Inject constructor(): ComponentMapper {
    override fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        return ComponentMapperResult(
            contributors = emptyMap(),
            noOwnerData = apks.flatMap { it.others }.toSet()
        )
    }
}
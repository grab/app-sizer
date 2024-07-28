package com.grab.sizer.analyzer.mapper


import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject

internal class OtherComponentMapper @Inject constructor() : ComponentMapper {
    override fun Set<ApkFileInfo>.mapTo(aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult {
        // Todo: Add logic to map others
        return ComponentMapperResult(
            contributors = emptyMap(),
            noOwnerData = flatMap { it.others }.toSet()
        )
    }
}
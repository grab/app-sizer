package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo

/**
 * A map contain the input and the files belong to it
 */
internal typealias RawContributors = Map<String, Set<FileInfo>>

internal data class ComponentMapperResult(
    val noOwnerData: Set<FileInfo>,
    val contributors: RawContributors
)

internal interface ComponentMapper {
    fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult
}




package com.grab.tools.analyzer.mapper

import com.grab.tools.analyzer.model.FileInfo
import com.grab.tools.parser.AarFileInfo
import com.grab.tools.parser.ApkFileInfo
import com.grab.tools.parser.JarFileInfo

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




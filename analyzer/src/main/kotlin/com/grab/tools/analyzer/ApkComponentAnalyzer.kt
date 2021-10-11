package com.grab.tools.analyzer

import com.grab.tools.FileInfo
import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo

/**
 * A map between the input and the files belong to it
 */
typealias RawContributors = Map<String, Set<FileInfo>>

data class ComponentAnalyzerResult(
    val noOwnerData: Set<FileInfo>,
    val contributors: RawContributors
)

interface ApkComponentAnalyzer {
    fun analyze(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentAnalyzerResult
}




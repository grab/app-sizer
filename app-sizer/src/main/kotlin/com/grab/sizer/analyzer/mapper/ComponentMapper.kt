package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo

/**
 * Type alias for a map containing input files (aar/jar) and the set of files associated with each input.
 */
internal typealias RawContributors = Map<BinaryFileInfo, Set<FileInfo>>


/**
 * Holds the results of a component mapping process.
 * Contains a set of non-owned component files and raw contributors with their associated FileInfo.
 *
 * @property noOwnerData Set of FileInfo representing files not owned by any contributor.
 * @property contributors Map of raw contributors and their associated FileInfo.
 */
internal data class ComponentMapperResult(
    val noOwnerData: Set<FileInfo>,
    val contributors: RawContributors
)

/**
 * Interface for component-specific mappers that target specific file types such as resources, assets, or native libraries, etc.
 * The implementation maps files from APKs to aar & jar files and outputs a ComponentMapperResult.
 */
internal interface ComponentMapper {
    /**
     * Maps files from the provided APKs to AARs and JARs
     * Outputs a ComponentMapperResult with mapped contributors and files that can not find an owner.
     *
     * @param aars The set of AAR files to analyze.
     * @param jars The set of JAR files to analyze.
     * @return a ComponentMapperResult which contains a map of aar/jar file to its own set of FileInfo.
     */
    fun Set<ApkFileInfo>.mapTo(aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentMapperResult
}




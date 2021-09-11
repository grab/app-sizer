/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

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




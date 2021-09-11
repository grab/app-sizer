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

import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.castToClass
import com.grab.sizer.analyzer.model.castToRawFile
import com.grab.sizer.di.AnalyzerClass
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.BinaryFileInfo
import com.grab.sizer.parser.JarFileInfo
import javax.inject.Inject


internal interface ApkComponentProcessor {
    /**
     * Processes a set of apk, aar and jar files. This method maps contributors (aar & jar files) to the apk files.
     * Analyses the given files and returns a ComponentProcessorResult containing a set of Contributor objects.
     * Each Contributor object represents an aar or jar file and the components it contributes to an apk files.
     *
     * @param apks the set of APK files to process
     * @param aars the set of AAR files to process
     * @param jars the set of JAR files to process
     * @return a ComponentProcessorResult containing a list of Contributors and non-owned component files
     */
    fun process(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentProcessorResult
}

/**
 * Holds the result of processing apk, aar, and jar files in ApkComponentProcessor.
 * Contains sets of Contributors and non-owned component files.
 *
 * @property contributors a set of Contributor objects each representing an aar or jar file and its contribution to an apk file.
 * @property noOwnerAssets a set of FileInfo objects representing assets not owned by any contributor.
 * @property noOwnerResources a set of FileInfo objects representing resources not owned by any contributor.
 * @property noOwnerNativeLibs a set of FileInfo objects representing native libs not owned by any contributor.
 * @property noOwnerClasses a set of FileInfo objects representing classes not owned by any contributor.
 * @property noOwnerOthers a set of FileInfo objects representing other components not owned by any contributor.
 */
internal data class ComponentProcessorResult(
    val contributors: Set<Contributor>,
    val noOwnerAssets: Set<FileInfo>,
    val noOwnerResources: Set<FileInfo>,
    val noOwnerNativeLibs: Set<FileInfo>,
    val noOwnerClasses: Set<FileInfo>,
    val noOwnerOthers: Set<FileInfo>,
)

internal class DefaultApkComponentProcessor @Inject constructor(private val mappers: Map<AnalyzerClass, @JvmSuppressWildcards ComponentMapper>) :
    ApkComponentProcessor {

    override fun process(
        apks: Set<ApkFileInfo>,
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentProcessorResult {
        val rawContributorMap = mappers.mapValues {
            with(it.value) {
                apks.mapTo(aars, jars)
            }
        }
        val contributors = mutableMapOf<BinaryFileInfo, Contributor>().apply {
            createAssetContributors(rawContributorMap)
            createResourceContributors(rawContributorMap)
            createNativeLibsContributors(rawContributorMap)
            createOtherContributors(rawContributorMap)
            createClassContributors(rawContributorMap)
        }.values.toSet()
        return ComponentProcessorResult(
            contributors = contributors,
            noOwnerAssets = rawContributorMap.getNoOwnerData(AssetComponentMapper::class.java),
            noOwnerResources = rawContributorMap.getNoOwnerData(ResourceComponentMapper::class.java),
            noOwnerNativeLibs = rawContributorMap.getNoOwnerData(NativeLibComponentMapper::class.java),
            noOwnerClasses = rawContributorMap.getNoOwnerData(ClassComponentMapper::class.java),
            noOwnerOthers = rawContributorMap.getNoOwnerData(OtherComponentMapper::class.java),
        )
    }

    private fun Map<AnalyzerClass, ComponentMapperResult>.getNoOwnerData(clazz: Class<*>): Set<FileInfo> =
        get(clazz)?.noOwnerData ?: emptySet()

    private fun MutableMap<BinaryFileInfo, Contributor>.createAssetContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[AssetComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val lib = rawEntry.key
            val assets = rawEntry.value
            var contributor = get(lib)
            contributor = contributor?.copy(assets = assets.castToRawFile())
                ?: Contributor(originalOwner = lib, assets = assets.castToRawFile())
            put(lib, contributor)
        }
    }

    private fun MutableMap<BinaryFileInfo, Contributor>.createResourceContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[ResourceComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val lib = rawEntry.key
            val data = rawEntry.value
            var contributor = get(lib)
            contributor = contributor?.copy(resources = data.castToRawFile())
                ?: Contributor(originalOwner = lib, resources = data.castToRawFile())
            put(lib, contributor)
        }
    }

    private fun MutableMap<BinaryFileInfo, Contributor>.createNativeLibsContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[NativeLibComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val lib = rawEntry.key
            val data = rawEntry.value
            var contributor = get(lib)
            contributor = contributor?.copy(nativeLibs = data.castToRawFile())
                ?: Contributor(originalOwner = lib, nativeLibs = data.castToRawFile())
            put(lib, contributor)
        }
    }

    private fun MutableMap<BinaryFileInfo, Contributor>.createOtherContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[OtherComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val lib = rawEntry.key
            val data = rawEntry.value
            var contributor = get(lib)
            contributor = contributor?.copy(others = data.castToRawFile())
                ?: Contributor(originalOwner = lib, others = data.castToRawFile())
            put(lib, contributor)
        }
    }

    private fun MutableMap<BinaryFileInfo, Contributor>.createClassContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[ClassComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val lib = rawEntry.key
            val data = rawEntry.value
            var contributor = get(lib)
            contributor = contributor?.copy(classes = data.castToClass())
                ?: Contributor(originalOwner = lib, classes = data.castToClass())
            put(lib, contributor)
        }
    }
}



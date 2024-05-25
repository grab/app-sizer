package com.grab.sizer.analyzer.mapper

import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.di.AnalyzerClass
import com.grab.sizer.parser.JarFileInfo
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.analyzer.model.castToClass
import com.grab.sizer.analyzer.model.castToRawFile
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
        val rawContributorMap = mappers.mapValues { it.value.analyze(apks, aars, jars) }
        val contributors = mutableMapOf<String, Contributor>().apply {
            createAssetContributors(rawContributorMap)
            createResourceContributors(rawContributorMap)
            createNativeLibsContributors(rawContributorMap)
            createOtherContributors(rawContributorMap)
            createClassContributors(rawContributorMap)
        }.values.toSet()
        return ComponentProcessorResult(
            contributors = contributors,
            noOwnerAssets = rawContributorMap.getNoOwnerData(AssetsComponentMapper::class.java),
            noOwnerResources = rawContributorMap.getNoOwnerData(ResourceComponentMapper::class.java),
            noOwnerNativeLibs = rawContributorMap.getNoOwnerData(NativeLibComponentMapper::class.java),
            noOwnerClasses = rawContributorMap.getNoOwnerData(ClassesComponentMapper::class.java),
            noOwnerOthers = rawContributorMap.getNoOwnerData(OtherComponentMapper::class.java),
        )
    }

    private fun Map<AnalyzerClass, ComponentMapperResult>.getNoOwnerData(clazz: Class<*>): Set<FileInfo> =
        get(clazz)?.noOwnerData ?: emptySet()

    private fun MutableMap<String, Contributor>.createAssetContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[AssetsComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val assets = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(assets = assets.castToRawFile())
                ?: Contributor(path = libName, assets = assets.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createResourceContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[ResourceComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(resources = data.castToRawFile())
                ?: Contributor(path = libName, resources = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createNativeLibsContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[NativeLibComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(nativeLibs = data.castToRawFile())
                ?: Contributor(path = libName, nativeLibs = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createOtherContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[OtherComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(others = data.castToRawFile())
                ?: Contributor(path = libName, others = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createClassContributors(rawContributorMap: Map<AnalyzerClass, ComponentMapperResult>) {
        rawContributorMap[ClassesComponentMapper::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(classes = data.castToClass())
                ?: Contributor(path = libName, classes = data.castToClass())
            put(libName, contributor)
        }
    }
}



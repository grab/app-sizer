package com.grab.tools.analyzer.apk

import com.grab.tools.aar.AarFileInfo
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.di.AnalyzerClass
import com.grab.tools.jar.JarFileInfo
import com.grab.tools.model.Contributor
import com.grab.tools.model.FileInfo
import com.grab.tools.model.castToClass
import com.grab.tools.model.castToRawFile
import javax.inject.Inject


interface ApkComponentProcessor {
    fun process(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): ComponentProcessorResult
}

data class ComponentProcessorResult(
    val contributors: Set<Contributor>,
    val noOwnerAssets: Set<FileInfo>,
    val noOwnerResources: Set<FileInfo>,
    val noOwnerNativeLibs: Set<FileInfo>,
    val noOwnerClasses: Set<FileInfo>,
    val noOwnerOthers: Set<FileInfo>,
)

class DefaultApkComponentProcessor @Inject constructor(private val analytics: Map<AnalyzerClass, @JvmSuppressWildcards ApkComponentAnalyzer>) :
    ApkComponentProcessor {

    override fun process(
        apks: Set<ApkFileInfo>,
        aars: Set<AarFileInfo>,
        jars: Set<JarFileInfo>
    ): ComponentProcessorResult {
        val rawContributorMap = analytics.mapValues { it.value.analyze(apks, aars, jars) }
        val contributors = mutableMapOf<String, Contributor>().apply {
            createAssetContributors(rawContributorMap)
            createResourceContributors(rawContributorMap)
            createNativeLibsContributors(rawContributorMap)
            createOtherContributors(rawContributorMap)
            createClassContributors(rawContributorMap)
        }.values.toSet()
        return ComponentProcessorResult(
            contributors = contributors,
            noOwnerAssets = rawContributorMap.getNoOwnerData(AssetsApkComponentAnalyzer::class.java),
            noOwnerResources = rawContributorMap.getNoOwnerData(ResourceApkComponentAnalyzer::class.java),
            noOwnerNativeLibs = rawContributorMap.getNoOwnerData(NativeLibApkComponentAnalyzer::class.java),
            noOwnerClasses = rawContributorMap.getNoOwnerData(ClassesApkComponentAnalyzer::class.java),
            noOwnerOthers = rawContributorMap.getNoOwnerData(OtherApkComponentAnalyzer::class.java),
        )
    }

    private fun Map<AnalyzerClass, ComponentAnalyzerResult>.getNoOwnerData(clazz: Class<*>): Set<FileInfo> =
        get(clazz)?.noOwnerData ?: emptySet()

    private fun MutableMap<String, Contributor>.createAssetContributors(rawContributorMap: Map<AnalyzerClass, ComponentAnalyzerResult>) {
        rawContributorMap[AssetsApkComponentAnalyzer::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val assets = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(assets = assets.castToRawFile())
                ?: Contributor(path = libName, assets = assets.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createResourceContributors(rawContributorMap: Map<AnalyzerClass, ComponentAnalyzerResult>) {
        rawContributorMap[ResourceApkComponentAnalyzer::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(resources = data.castToRawFile())
                ?: Contributor(path = libName, resources = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createNativeLibsContributors(rawContributorMap: Map<AnalyzerClass, ComponentAnalyzerResult>) {
        rawContributorMap[NativeLibApkComponentAnalyzer::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(nativeLibs = data.castToRawFile())
                ?: Contributor(path = libName, nativeLibs = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createOtherContributors(rawContributorMap: Map<AnalyzerClass, ComponentAnalyzerResult>) {
        rawContributorMap[OtherApkComponentAnalyzer::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(others = data.castToRawFile())
                ?: Contributor(path = libName, others = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createClassContributors(rawContributorMap: Map<AnalyzerClass, ComponentAnalyzerResult>) {
        rawContributorMap[ClassesApkComponentAnalyzer::class.java]?.contributors?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(classes = data.castToClass())
                ?: Contributor(path = libName, classes = data.castToClass())
            put(libName, contributor)
        }
    }
}



package com.grab.tools

import com.grab.tools.aar.AarFileInfo
import com.grab.tools.analyzer.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.jar.JarFileInfo


class ApkComponentAnalytic(private val analytics: Map<AnalyzerClass, ApkComponentAnalyzer>) {

    fun process(apks: Set<ApkFileInfo>, aars: Set<AarFileInfo>, jars: Set<JarFileInfo>): Set<Contributor> {
        val rawContributorMap = analytics.mapValues { it.value.analyze(apks, aars, jars) }
        return mutableMapOf<String, Contributor>().apply {
            createAssetContributors(rawContributorMap)
            createResourceContributors(rawContributorMap)
            createNativeLibsContributors(rawContributorMap)
            createOtherContributors(rawContributorMap)
            createClassContributors(rawContributorMap)
        }.values.toSet()
    }

    private fun MutableMap<String, Contributor>.createAssetContributors(rawContributorMap: Map<AnalyzerClass, RawContributors>) {
        rawContributorMap[AssetsApkComponentAnalyzer::class.java]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val assets = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(assets = assets.castToRawFile())
                ?: Contributor(path = libName, assets = assets.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createResourceContributors(rawContributorMap: Map<AnalyzerClass, RawContributors>) {
        rawContributorMap[ResourceApkComponentAnalyzer::class.java]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(resources = data.castToRawFile())
                ?: Contributor(path = libName, resources = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createNativeLibsContributors(rawContributorMap: Map<AnalyzerClass, RawContributors>) {
        rawContributorMap[NativeLibApkComponentAnalyzer::class.java]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(nativeLibs = data.castToRawFile())
                ?: Contributor(path = libName, nativeLibs = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createOtherContributors(rawContributorMap: Map<AnalyzerClass, RawContributors>) {
        rawContributorMap[OtherApkComponentAnalyzer::class.java]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(others = data.castToRawFile())
                ?: Contributor(path = libName, others = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createClassContributors(rawContributorMap: Map<AnalyzerClass, RawContributors>) {
        rawContributorMap[ClassesApkComponentAnalyzer::class.java]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(classes = data.castToClass())
                ?: Contributor(path = libName, classes = data.castToClass())
            put(libName, contributor)
        }
    }

    private fun Set<FileInfo>.castToClass(): Set<ClassFileInfo> = filterIsInstance<ClassFileInfo>().toSet()
    private fun Set<FileInfo>.castToRawFile(): Set<RawFileInfo> = filterIsInstance<RawFileInfo>().toSet()
}



package com.grab.bundle

import com.grab.bundle.aar.AarFileInfo
import com.grab.bundle.analyzer.*
import com.grab.bundle.apk.ApkFileInfo
import com.grab.bundle.jar.JarFileInfo


class ApkComponentAnalytic(private val analytics: Map<String, Analyzer>) {

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

    private fun MutableMap<String, Contributor>.createAssetContributors(rawContributorMap: Map<String, RawContributors>) {
        rawContributorMap[AssetsAnalyzer.TAG]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val assets = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(assets = assets.castToRawFile())
                ?: Contributor(name = libName, assets = assets.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createResourceContributors(rawContributorMap: Map<String, RawContributors>) {
        rawContributorMap[ResourceAnalyzer.TAG]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(resources = data.castToRawFile())
                ?: Contributor(name = libName, resources = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createNativeLibsContributors(rawContributorMap: Map<String, RawContributors>) {
        rawContributorMap[NativeLibAnalyzer.TAG]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(nativeLibs = data.castToRawFile())
                ?: Contributor(name = libName, nativeLibs = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createOtherContributors(rawContributorMap: Map<String, RawContributors>) {
        rawContributorMap[OtherAnalyzer.TAG]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(others = data.castToRawFile())
                ?: Contributor(name = libName, others = data.castToRawFile())
            put(libName, contributor)
        }
    }

    private fun MutableMap<String, Contributor>.createClassContributors(rawContributorMap: Map<String, RawContributors>) {
        rawContributorMap[ClassesAnalyzer.TAG]?.forEach { rawEntry ->
            val libName = rawEntry.key
            val data = rawEntry.value
            var contributor = get(libName)
            contributor = contributor?.copy(classes = data.castToClass())
                ?: Contributor(name = libName, classes = data.castToClass())
            put(libName, contributor)
        }
    }

    private fun Set<FileInfo>.castToClass(): Set<ClassFileInfo> = filterIsInstance<ClassFileInfo>().toSet()
    private fun Set<FileInfo>.castToRawFile(): Set<RawFileInfo> = filterIsInstance<RawFileInfo>().toSet()
}



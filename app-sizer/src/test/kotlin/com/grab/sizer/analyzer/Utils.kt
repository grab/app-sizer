package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.*
import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.parser.*

class FakeDataPasser(
    override val apks: MutableSet<ApkFileInfo> = mutableSetOf(),
    override val libAars: MutableSet<AarFileInfo> = mutableSetOf(),
    override val libJars: MutableSet<JarFileInfo> = mutableSetOf(),
    override val moduleAars: MutableSet<AarFileInfo> = mutableSetOf(),
    override val moduleJars: MutableSet<JarFileInfo> = mutableSetOf()
) : DataParser


internal class MapperComponent {
    private val mapOfComponentMapper = mapOf(
        ResourceComponentMapper::class to ResourceComponentMapper(),
        NativeLibComponentMapper::class to NativeLibComponentMapper(),
        AssetComponentMapper::class to AssetComponentMapper(),
        ClassComponentMapper::class to ClassComponentMapper(),
        OtherComponentMapper::class to OtherComponentMapper(),
    )

    val apkComponentProcessor = DefaultApkComponentProcessor(mapOfComponentMapper.mapKeys { (k, _) -> k.java })
}


internal fun createRawFileInfo(path: String, downloadSize: Long = 50, size: Long = 150) =
    RawFileInfo(path = path, downloadSize = downloadSize, size = size)

internal fun createEmptyDexFileInfo(name: String = "dex"): DexFileInfo = DexFileInfo(
    name = name,
    downloadSize = 100,
    size = 200,
    classes = emptySet(),
)

internal fun createClassFileInfo(name: String, downloadSize: Long = 100, size: Long = 300) =
    ClassFileInfo(name = name, downloadSize = downloadSize, size = size)
package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.createRawFileInfo
import com.grab.sizer.parser.AarFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.JarFileInfo
import org.junit.Assert
import org.junit.Test

class AssetComponentMapperTest {
    private val asset1 = createRawFileInfo(path = "/assets/asset_resource_1.xml")
    private val asset2 = createRawFileInfo(path = "/assets/asset_resource_2.xml")
    private val asset3 = createRawFileInfo(path = "/assets/asset_resource_3.xml")
    private val noOwnerAsset = createRawFileInfo(path = "/assets/asset_resource_5.xml")

    @Test
    fun assetsAnalyzerShouldResultNoOwnerAssetProperly() {
        val apk = createEmptyApkInfo().copy(
            assets = setOf(asset1.copy(), asset2.copy(), noOwnerAsset.copy())
        )
        val aar1 = createEmptyAar("aar1").copy(assets = setOf(asset1.copy()))
        val aar2 = createEmptyAar("aar2").copy(assets = setOf(asset2.copy(), asset3.copy()))
        val aar3 = createEmptyAar("aar3")
        val jar1 = createEmptyJar("jar1")
        val result = AssetComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2, aar3), setOf(jar1))
        }
        Assert.assertTrue(result.noOwnerData.contains(noOwnerAsset))
        Assert.assertFalse(result.noOwnerData.contains(asset1))
    }

    @Test
    fun assetsAnalyzerShouldResultTheProperAssetFilesContributedToApk() {
        val apk = createEmptyApkInfo().copy(
            assets = setOf(asset1.copy(), asset2.copy(), noOwnerAsset.copy())
        )
        val aar1 = createEmptyAar("aar1").copy(assets = setOf(asset1.copy()))
        val aar2 = createEmptyAar("aar2").copy(assets = setOf(asset2.copy(), asset3.copy()))
        val aar3 = createEmptyAar("aar3")
        val jar1 = createEmptyJar("jar1")
        val result = AssetComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2, aar3), setOf(jar1))
        }

        Assert.assertFalse(result.contributors.containsKey(aar3))
        Assert.assertFalse(result.contributors.containsKey(jar1))

        Assert.assertEquals(1, result.contributors[aar2]?.size)
        Assert.assertEquals(asset2, result.contributors[aar2]?.first())
        Assert.assertEquals(asset1, result.contributors[aar1]?.first())
    }
}

internal fun createEmptyJar(name: String = "jar", path: String = "jar/path"): JarFileInfo = JarFileInfo(
    name = name,
    path = "$path/$name",
    classes = emptySet(),
    nativeLibs = emptySet(),
    others = emptySet()
)

internal fun createEmptyAar(name: String = "aar", path: String = "aar/path"): AarFileInfo = AarFileInfo(
    name = name,
    path = "$path/$name.aar",
    resources = emptySet(),
    nativeLibs = emptySet(),
    assets = emptySet(),
    others = emptySet(),
    jars = emptySet()
)

internal fun createEmptyApkInfo(name: String = "apk1"): ApkFileInfo =
    ApkFileInfo(
        name = name,
        resources = emptySet(),
        nativeLibs = emptySet(),
        others = emptySet(),
        dexes = emptySet(),
        assets = emptySet(),
    )
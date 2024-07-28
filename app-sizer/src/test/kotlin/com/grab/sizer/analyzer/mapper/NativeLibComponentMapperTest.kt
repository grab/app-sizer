package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.createRawFileInfo
import org.junit.Assert
import org.junit.Test

class NativeLibComponentMapperTest {
    private val nativeLib1 = createRawFileInfo(path = "/lib/armeabi-v7a/sample.so")
    private val nativeLib2 = createRawFileInfo(path = "/lib/armeabi-v7a/sample2.so")
    private val nativeLib3 = createRawFileInfo(path = "/lib/armeabi-v7a/sample3.so")
    private val nativeLib1Aar = createRawFileInfo(path = "/jni/armeabi-v7a/sample.so")
    private val noOwnerLib = createRawFileInfo(path = "/lib/armeabi-v7a/noOwnerLib.so")

    @Test
    fun nativeLibMapperShouldResultProperContributors() {
        val apk = createEmptyApkInfo().copy(
            nativeLibs = setOf(nativeLib3.copy(), nativeLib2.copy(), noOwnerLib.copy())
        )

        val aar1 = createEmptyAar("aar1").copy(
            nativeLibs = setOf(nativeLib2.copy()) // path is different
        )

        val aar2 = createEmptyAar("aar2").copy(nativeLibs = setOf(nativeLib3.copy()))

        val result = NativeLibComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(2, result.contributors.size)
        Assert.assertNotNull(result.contributors[aar1])
        Assert.assertNotNull(result.contributors[aar2])
        Assert.assertEquals(nativeLib2, result.contributors[aar1]?.first())
        Assert.assertEquals(nativeLib3, result.contributors[aar2]?.first())
    }

    @Test
    fun nativeLibMapperShouldHandleDiffOnPathBetweenApkAndJar() {
        /**
         * There are different between APK and AAR native file path.
         * This method will remove the pre-fix path for the so file, to ensure the mapping working as expected
         * Example,
         * APK: /lib/armeabi-v7a/sample.so
         * AAR: /jni/armeabi-v7a/sample.so
         */
        val apk = createEmptyApkInfo().copy(
            nativeLibs = setOf(nativeLib1.copy(), nativeLib2.copy(), noOwnerLib.copy())
        )

        val aar1 = createEmptyAar("aar1").copy(
            nativeLibs = setOf(nativeLib1Aar.copy()) // path is different with nativeLib1
        )

        val aar2 = createEmptyAar("aar2").copy(nativeLibs = setOf(nativeLib3.copy()))

        val result = NativeLibComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(1, result.contributors.size)
        Assert.assertNotNull(result.contributors[aar1])
        Assert.assertEquals(nativeLib1, result.contributors[aar1]?.first())
    }

    @Test
    fun nativeLibMapperShouldNotResultNoUseNativeLib() {
        val apk = createEmptyApkInfo().copy(
            nativeLibs = setOf(nativeLib1.copy(), nativeLib2.copy(), noOwnerLib.copy())
        )

        val aar1 = createEmptyAar("aar1").copy(
            nativeLibs = setOf(nativeLib1Aar.copy()) // path is different
        )

        val aar2 = createEmptyAar("aar2").copy(nativeLibs = setOf(nativeLib3.copy()))

        val result = NativeLibComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertNull(result.contributors[aar2])
    }

    @Test
    fun nativeLibMapperShouldResultProperNoOwnerLip() {
        val apk = createEmptyApkInfo().copy(
            nativeLibs = setOf(nativeLib1.copy(), noOwnerLib.copy())
        )

        val aar1 = createEmptyAar("aar1").copy(
            nativeLibs = setOf(nativeLib1Aar.copy()) // path is different
        )

        val aar2 = createEmptyAar("aar2").copy(nativeLibs = setOf(nativeLib3.copy()))

        val result = NativeLibComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(1, result.noOwnerData.size)
        Assert.assertEquals(noOwnerLib, result.noOwnerData.first())
    }
}
package com.grab.sizer.analyzer.mapper

import com.grab.sizer.analyzer.createRawFileInfo
import com.grab.sizer.analyzer.model.RawFileInfo
import org.junit.Assert
import org.junit.Test

class ResourceComponentMapperTest {
    private val resourceDrawable = createRawFileInfo(path = "/res/drawable-xlarge-port-hdpi-v4/ic_test.xml")
    private val resourceAnimation = createRawFileInfo(path = "/res/animator/test_animator.xml")
    private val resourceFont = createRawFileInfo(path = "/res/font/test_font.xml")
    private val noOwnerResource = createRawFileInfo(path = "/res/layout/test_layout.xml")

    @Test
    fun resourceMapperShouldResultProperContributors() {
        val apk = createEmptyApkInfo().copy(
            resources = setOf(
                resourceDrawable.copy(),
                resourceAnimation.copy(),
                resourceFont.copy(),
                noOwnerResource.copy()
            )
        )

        val aar1 = createEmptyAar("aar1").copy(
            resources = setOf(resourceDrawable.copy(), resourceAnimation.copy())
        )

        val aar2 = createEmptyAar("aar2").copy(
            resources = setOf(
                resourceFont.copy()
            )
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(2, result.contributors.size)
        Assert.assertEquals(true, result.contributors[aar1]?.contains(resourceDrawable))
        Assert.assertEquals(true, result.contributors[aar1]?.contains(resourceAnimation))
        Assert.assertEquals(true, result.contributors[aar2]?.contains(resourceFont))
    }

    @Test
    fun resourceMapperShouldFlagOutNoOwnerItem() {
        val apk = createEmptyApkInfo().copy(
            resources = setOf(
                resourceDrawable.copy(),
                resourceAnimation.copy(),
                resourceFont.copy(),
                noOwnerResource.copy()
            )
        )

        val aar1 = createEmptyAar("aar1").copy(
            resources = setOf(resourceDrawable.copy(), resourceAnimation.copy())
        )

        val aar2 = createEmptyAar("aar2").copy(
            resources = setOf(
                resourceFont.copy()
            )
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(1, result.noOwnerData.size)
        Assert.assertEquals(noOwnerResource, result.noOwnerData.first())
    }

    @Test
    fun resourceMapperShouldNotIncludeNoUseResource() {
        val apk = createEmptyApkInfo().copy(
            resources = setOf(
                resourceDrawable.copy(),
                noOwnerResource.copy(),
                resourceFont.copy()
            )
        )

        val aar1 = createEmptyAar("aar1").copy(
            resources = setOf(resourceDrawable.copy(), resourceAnimation.copy())
        )

        val aar2 = createEmptyAar("aar2").copy(
            resources = setOf(
                resourceFont.copy()
            )
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), emptySet())
        }
        Assert.assertEquals(1, result.contributors[aar1]?.size)
        Assert.assertEquals(false, result.contributors[aar1]?.contains(resourceAnimation))
    }

    @Test
    fun classMapperShouldHandleDiffOnPathWithSpecialCharacter() {
        /**
         * There are cases the resources files are renamed, not sure why and how.
         * Here is an example: "/res/drawable/$bg_network_error__0.xml"
         */
        val apkResourceWithSpecialChar = createRawFileInfo(path = "/res/drawable/\$bg_network_error__0.xml")

        val apk = createEmptyApkInfo().copy(
            resources = setOf(apkResourceWithSpecialChar, resourceDrawable)
        )

        val aarResource = createRawFileInfo(path = "/res/drawable/bg_network_error.xml")

        val aar1 = createEmptyAar().copy(
            resources = setOf(aarResource)
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1), emptySet())
        }
        Assert.assertEquals(1, result.contributors.size)
        Assert.assertNotNull(result.contributors[aar1])
        Assert.assertEquals(apkResourceWithSpecialChar, result.contributors[aar1]?.first())
    }

    @Test
    fun classMapperShouldHandleDiffOnPathMinSupportSdkVersion() {
        /**
         * There are cases the resource directory were added with the min support sdk version
         * Ex : /res/drawable-v22/ic_geo_pickup_notes.xml
         */
        val apkResourceWithSpecialChar = createRawFileInfo(path = "/res/drawable-v22/ic_geo_pickup_notes.xml")

        val apk = createEmptyApkInfo().copy(
            resources = setOf(apkResourceWithSpecialChar, resourceDrawable)
        )

        val aarResource = createRawFileInfo(path = "/res/drawable/ic_geo_pickup_notes.xml")

        val aar1 = createEmptyAar().copy(
            resources = setOf(aarResource)
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1), emptySet())
        }
        Assert.assertEquals(1, result.contributors.size)
        Assert.assertNotNull(result.contributors[aar1])
        Assert.assertEquals(apkResourceWithSpecialChar, result.contributors[aar1]?.first())
    }

    @Test
    fun classMapperShouldHandleDiffOnPathForAllCases() {
        /**
         * Handle both the $ sign and version extension
         */
        val apkResourceWithSpecialChar = createRawFileInfo(path = "/res/drawable-v22/\$bg_network_error__0.xml")

        val apk = createEmptyApkInfo().copy(
            resources = setOf(apkResourceWithSpecialChar, resourceDrawable.copy())
        )

        val aarResource = createRawFileInfo(path = "/res/drawable/bg_network_error.xml")

        val aar1 = createEmptyAar().copy(
            resources = setOf(aarResource)
        )

        val result = ResourceComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1), emptySet())
        }
        Assert.assertEquals(1, result.contributors.size)
        Assert.assertNotNull(result.contributors[aar1])
        Assert.assertEquals(apkResourceWithSpecialChar, result.contributors[aar1]?.first())
    }
}
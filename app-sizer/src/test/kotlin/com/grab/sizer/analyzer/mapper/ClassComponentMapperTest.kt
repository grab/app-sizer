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

import com.grab.sizer.analyzer.createClassFileInfo
import com.grab.sizer.analyzer.createEmptyDexFileInfo
import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DexFileInfo
import org.junit.Assert
import org.junit.Test

class ClassComponentMapperTest {
    private val class1 = createClassFileInfo(name = "com.grab.test.HelloWorld")
    private val class2 = createClassFileInfo(name = "com.grab.test.TestClass2")
    private val class3 = createClassFileInfo(name = "com.grab.test.TestClass3")
    private val noOwnerClass = createClassFileInfo(name = "com.grab.test.TestClassNoOwner")

    @Test
    fun classMapperShouldHandleGeneratedInnerLambda() {
        val lambdaClassInApk1 = createClassFileInfo(
            name = "androidx.appcompat.app.AppCompatDelegateImpl\$Api24Impl"
        )
        val lambdaClassInApk2 = createClassFileInfo(
            name = "androidx.appcompat.app.AppCompatDelegate\$\$ExternalSyntheticLambda0"
        )

        val dex1 = createEmptyDexFileInfo().copy(
            classes = setOf(lambdaClassInApk1, lambdaClassInApk2)
        )
        val apk = createEmptyApkInfo().copy(dexes = setOf(dex1))

        val classInAar1 = createClassFileInfo(name = "androidx.appcompat.app.AppCompatDelegateImpl")
        val classInAar2 = createClassFileInfo(name = "androidx.appcompat.app.AppCompatDelegate")
        val classInAar3 = createClassFileInfo(name = "androidx.appcompat.app.AppLocalesStorageHelper")
        val aar = createEmptyAar("aar").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(classInAar1, classInAar2, classInAar3)
                )
            )
        )

        val result = ClassComponentMapper().run {
            setOf(apk).mapTo(setOf(aar), emptySet())
        }
        Assert.assertEquals(0, result.noOwnerData.size)
        Assert.assertTrue(result.contributors.containsKey(aar))
        Assert.assertEquals(true, result.contributors[aar]?.contains(lambdaClassInApk1))
        Assert.assertEquals(true, result.contributors[aar]?.contains(lambdaClassInApk2))
    }

    @Test
    fun classMapperShouldHandleGeneratedLambdaProperly() {
        val lambdaClassInApk = createClassFileInfo(
            name = "androidx.core.widget.-\$\$Lambda\$ContentLoadingProgressBar\$aW9csiS0dCdsR2nrqov9CuXAmGo"
        )
        val dex1 = createEmptyDexFileInfo().copy(
            classes = setOf(lambdaClassInApk)
        )
        val apk = createEmptyApkInfo().copy(dexes = setOf(dex1))

        val classInAar = createClassFileInfo(name = "androidx.core.widget.ContentLoadingProgressBar")
        val aar = createEmptyAar("aar").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(classInAar)
                )
            )
        )

        val result = ClassComponentMapper().run {
            setOf(apk).mapTo(setOf(aar), emptySet())
        }
        Assert.assertEquals(0, result.noOwnerData.size)
        Assert.assertTrue(result.contributors.containsKey(aar))
        Assert.assertEquals(true, result.contributors[aar]?.contains(lambdaClassInApk))
    }

    @Test
    fun classMapperShouldResultNoOwnerClassProperly() {
        val apk = createApkWithClass1Class2AndNoOwner()
        val aar1 = createAarWithClass1AndClass3()
        val jar1 = createEmptyJar().copy(classes = setOf(class2))
        val result = ClassComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1), setOf(jar1))
        }
        Assert.assertEquals(1, result.noOwnerData.size)
        Assert.assertEquals(noOwnerClass, result.noOwnerData.first())
    }

    private fun createAarWithClass1AndClass3() = createEmptyAar("Class1AndClass3").copy(
        jars = setOf(
            createEmptyJar().copy(
                classes = setOf(class1, class3)
            )
        )
    )

    private fun createApkWithClass1Class2AndNoOwner(): ApkFileInfo {
        val dex1 = createEmptyDexFileInfo().copy(
            classes = setOf(class1)
        )
        val dex2 = createEmptyDexFileInfo().copy(
            classes = setOf(class2, noOwnerClass)
        )
        return createEmptyApkInfo().copy(
            dexes = setOf(dex1, dex2)
        )
    }

    @Test
    fun classMapperShouldResultTheProperBinaryContributedToApk() {
        val apk = createApkWithClass1Class2AndNoOwner()
        val aar1 = createEmptyAar("aar1").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(class1)
                )
            )
        )
        val aar2 = createEmptyAar("aar2").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(class3)
                )
            )
        )
        val jar1 = createEmptyJar().copy(
            classes = setOf(class2)
        )

        val result = ClassComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), setOf(jar1))
        }

        Assert.assertEquals(2, result.contributors.size)
        Assert.assertTrue(result.contributors.containsKey(aar1))
        Assert.assertTrue(result.contributors.containsKey(jar1))
        Assert.assertFalse(result.contributors.containsKey(aar2))
    }

    @Test
    fun classMapperShouldResultTheCorrectClassesContributedToApk() {
        val apk = createApkWithClass1Class2AndNoOwner()
        val aar1 = createEmptyAar("aar1").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(class1)
                )
            )
        )
        val aar2 = createEmptyAar("aar2").copy(
            jars = setOf(
                createEmptyJar().copy(
                    classes = setOf(class3)
                )
            )
        )
        val jar1 = createEmptyJar("jar1").copy(
            classes = setOf(class2)
        )

        val result = ClassComponentMapper().run {
            setOf(apk).mapTo(setOf(aar1, aar2), setOf(jar1))
        }

        Assert.assertEquals(2, result.contributors.size)
        Assert.assertEquals(true, result.contributors[aar1]?.contains(class1))
        Assert.assertEquals(true, result.contributors[jar1]?.contains(class2))
        Assert.assertNull(result.contributors[aar2])
    }
}

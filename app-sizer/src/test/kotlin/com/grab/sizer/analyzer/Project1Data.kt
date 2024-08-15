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

package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.createEmptyAar
import com.grab.sizer.analyzer.mapper.createEmptyApkInfo
import com.grab.sizer.analyzer.mapper.createEmptyJar
import com.grab.sizer.parser.DexFileInfo

private const val TEAM_1 = "team1"
private const val TEAM_2 = "team2"

open class Project1Data {
    private object Assets {
        val libAar1Asset1 = createRawFileInfo(path = "/assets/asset_resource_1.xml", downloadSize = 10, size = 30)
        val moduleAar1Asset1 = createRawFileInfo(path = "/assets/asset_resource_2.xml", downloadSize = 20, size = 60)
        val moduleAar2Asset1 = createRawFileInfo(path = "/assets/asset_resource_3.xml", downloadSize = 30, size = 90)
    }

    private object Classes {
        val libAar1Class1 = createClassFileInfo(name = "com.grab.test.HelloWorld", downloadSize = 5, size = 20)
        val libJar1Class1 = createClassFileInfo(name = "com.grab.test.TestClass2", downloadSize = 7, size = 30)
        val moduleAar2Class1 = createClassFileInfo(name = "com.grab.test.TestClass3", downloadSize = 9, size = 40)
        val moduleJar1Class1 = createClassFileInfo(name = "com.grab.test.TestClass4", downloadSize = 13, size = 60)
        val moduleJar2Class1 = createClassFileInfo(name = "com.grab.test.TestClass5", downloadSize = 18, size = 80)
    }

    private object NativeLibs {
        val libAar2NativeLib1 = createRawFileInfo(path = "/lib/armeabi-v7a/sample.so", downloadSize = 10, size = 20)
        val moduleAar1NativeLib1 =
            createRawFileInfo(path = "/lib/armeabi-v7a/sample2.so", downloadSize = 50, size = 100)
        val moduleAar2NativeLib1 = createRawFileInfo(path = "/lib/armeabi-v7a/sample3.so", downloadSize = 30, size = 90)
    }

    private object Resources {
        val libAar2Resource1 =
            createRawFileInfo(path = "/res/drawable-xlarge-port-hdpi-v4/ic_test.xml", downloadSize = 30, size = 90)
        val moduleAar2Resource1 =
            createRawFileInfo(path = "/res/animator/test_animator.xml", downloadSize = 20, size = 40)
        val moduleAar1Resource1 = createRawFileInfo(path = "/res/font/test_font.xml", downloadSize = 20, size = 40)
    }

    private object Others {
        val moduleJar2Other1 =
            createRawFileInfo(path = "play-services-detection.properties", downloadSize = 10, size = 20)
        val moduleAar1Other1 = createRawFileInfo(path = "build-data.properties", downloadSize = 10, size = 30)
    }

    private val dexFile = DexFileInfo(
        name = "dex1",
        downloadSize = 52,
        classes = setOf(
            Classes.libAar1Class1,
            Classes.libJar1Class1,
            Classes.moduleAar2Class1,
            Classes.moduleJar1Class1,
            Classes.moduleJar2Class1
        ),
        size = 200
    )

    /**
     * Total download size 292 bytes
     */
    private val apk = createEmptyApkInfo("apk1").copy(
        assets = setOf(Assets.libAar1Asset1, Assets.moduleAar1Asset1, Assets.moduleAar2Asset1), // 60b
        dexes = setOf(dexFile), // 52b
        nativeLibs = setOf(
            NativeLibs.libAar2NativeLib1,
            NativeLibs.moduleAar1NativeLib1,
            NativeLibs.moduleAar2NativeLib1
        ), // 90b
        resources = setOf(
            Resources.libAar2Resource1,
            Resources.moduleAar1Resource1,
            Resources.moduleAar2Resource1
        ), // 70b
        others = setOf(Others.moduleJar2Other1, Others.moduleAar1Other1) // 20b
    )

    /**
     * Total download size 15 bytes
     */
    val libAar1 = createEmptyAar("libAar1").copy(
        assets = setOf(Assets.libAar1Asset1),
        jars = setOf(createEmptyJar("jar").copy(classes = setOf(Classes.libAar1Class1)))
    )

    /**
     * Total download size 40 bytes
     */
    val libAar2 = createEmptyAar("libAar2").copy(
        resources = setOf(Resources.libAar2Resource1),
        nativeLibs = setOf(NativeLibs.libAar2NativeLib1)
    )

    /**
     * Total download size 7 bytes
     */
    val libJar1 = createEmptyJar("libJar").copy(
        classes = setOf(Classes.libJar1Class1)
    )

    /**
     * Total download size 90 bytes
     * Resources: 20 bytes
     * Assets: 20 bytes
     */
    open val moduleAar1 = createEmptyAar(name = "moduleAar1", path = "moduleAar1/build/outputs/aar").copy(
        nativeLibs = setOf(NativeLibs.moduleAar1NativeLib1),
        resources = setOf(Resources.moduleAar1Resource1),
        assets = setOf(Assets.moduleAar1Asset1),
        others = setOf(Others.moduleAar1Other1)
    )

    /**
     * Total download size 89 byte
     * Codebase: 9 byte
     * Resources: 20 bytes
     * Assets: 30 bytes
     */
    open val moduleAar2 = createEmptyAar(name = "moduleAar2", path = "moduleAar2/build/outputs/aar").copy(
        nativeLibs = setOf(NativeLibs.moduleAar2NativeLib1),
        assets = setOf(Assets.moduleAar2Asset1),
        resources = setOf(Resources.moduleAar2Resource1),
        jars = setOf(createEmptyJar("jar").copy(classes = setOf(Classes.moduleAar2Class1)))
    )

    /**
     * Total download size 13 byte
     * Codebase: 13 byte
     */
    open val moduleJar1 = createEmptyJar(name = "moduleJar1", path = "moduleJar1/build/libs").copy(
        classes = setOf(Classes.moduleJar1Class1)
    )

    /**
     * Total download size 28 byte
     * Codebase: 18 byte
     */
    open val moduleJar2 = createEmptyJar(name = "moduleJar2", path = "moduleJar2/build/libs").copy(
        classes = setOf(Classes.moduleJar2Class1),
        others = setOf(Others.moduleJar2Other1)
    )

    val fakeDataPasser = FakeDataPasser(
        apks = mutableSetOf(apk),
        libAars = mutableSetOf(libAar1, libAar2),
        libJars = mutableSetOf(libJar1),
        moduleAars = mutableSetOf(moduleAar1, moduleAar2),
        moduleJars = mutableSetOf(moduleJar1, moduleJar2)
    )

    val teamMapping = object : TeamMapping {
        override val teamToModuleMap: Map<String, List<String>> = mapOf(
            TEAM_1 to listOf(moduleAar1.name, moduleJar1.name),
            TEAM_2 to listOf(moduleAar2.name, moduleJar2.name),
        )
        override val moduleToTeamMap: Map<String, String> = mapOf(
            moduleAar1.name to TEAM_1,
            moduleJar1.name to TEAM_1,
            moduleAar2.name to TEAM_2,
            moduleJar2.name to TEAM_2
        )
    }
}
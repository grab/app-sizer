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

package com.grab.sizer

import com.grab.sizer.utils.DefaultFileQuery
import org.junit.Assert
import org.junit.Test
import java.io.File

class CliInputProviderTest {
    private val fileQuery = DefaultFileQuery()
    private val testingProject1 = TestingProject1()
    private val config = testingProject1.config


    @Test
    fun provideModuleAarShouldGetAllAarFromProjectFolderWhenModulesDirIsNotProjectRoot() {
        val config = config.copy(
            projectInput = config.projectInput.copy(projectRoot = File("./abc"))
        )

        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleAars = cliInputProvider.provideModuleAar()
            .toList()
            .sortedBy { it.file }
            .toTypedArray()
        val expectingAllAars = testingProject1.expectingAllAarsWhenNotAProjectRoot
            .sortedBy { it.file }
            .toTypedArray()
        Assert.assertEquals(expectingAllAars.size, 3)
        Assert.assertArrayEquals(moduleAars, expectingAllAars)
    }

    @Test
    fun provideModuleJarShouldGetAllJarFromProjectFolderWhenModulesDirIsNotProjectRoot() {
        val config = config.copy(
            projectInput = config.projectInput.copy(projectRoot = File("./abc"))
        )

        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleJars = cliInputProvider.provideModuleJar()
            .toList()
            .sortedBy { it.file }
            .toTypedArray()

        val expectingAllJars = testingProject1.expectingAllJarsWhenNotAProjectRoot.sortedBy { it.file }.toTypedArray()

        Assert.assertEquals(expectingAllJars.size, 2)
        Assert.assertArrayEquals(moduleJars, expectingAllJars)
    }

    @Test
    fun provideModuleAarShouldGetCorrectAarFromProjectFolderWhenModulesDirIsProjectRoot() {
        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleAars = cliInputProvider.provideModuleAar()
            .toList()
            .sortedBy { it.file }
            .toTypedArray()
        val expectingModuleAars = testingProject1.expectingModuleAars
            .sortedBy { it.tag }
            .toTypedArray()
        Assert.assertEquals(expectingModuleAars.size, 2)
        Assert.assertArrayEquals(moduleAars, expectingModuleAars)
    }

    @Test
    fun provideModuleJarShouldGetCorrectJarFromProjectFolderWhenModulesDirIsProjectRoot() {

        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleJars = cliInputProvider.provideModuleJar()
            .toList()
            .sortedBy { it.tag }
            .toTypedArray()
        val expectingModuleJars = testingProject1.expectingModuleJars
            .sortedBy { it.tag }
            .toTypedArray()
        Assert.assertEquals(expectingModuleJars.size, 1)
        Assert.assertArrayEquals(moduleJars, expectingModuleJars)
    }

    @Test
    fun provideLibraryAarShouldGetAllAarFromFolder() {
        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val libraryAar = cliInputProvider.provideLibraryAar().toList()
            .sortedBy { it.tag }
            .toTypedArray()
        val expectingLibAars = testingProject1.expectingLibAars
            .sortedBy { it.tag }
            .toTypedArray()
        Assert.assertEquals(expectingLibAars.size, 2)
        Assert.assertArrayEquals(libraryAar, expectingLibAars)
    }

    @Test
    fun provideLibraryJarShouldGetAllAarFromFolder() {
        val cliInputProvider = CliInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val libraryJars = cliInputProvider.provideLibraryJar()
            .toList()
            .sortedBy { it.tag }
            .toTypedArray()
        val expectingLibJars = testingProject1.expectingLibJars
            .sortedBy { it.tag }
            .toTypedArray()
        Assert.assertEquals(expectingLibJars.size, 2)
        Assert.assertArrayEquals(libraryJars, expectingLibJars)
    }

}
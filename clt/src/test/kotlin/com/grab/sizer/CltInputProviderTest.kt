package com.grab.sizer

import com.grab.sizer.utils.DefaultFileQuery
import org.junit.Assert
import org.junit.Test
import java.io.File

class CltInputProviderTest {
    private val fileQuery = DefaultFileQuery()
    private val testingProject1 = TestingProject1()
    private val config = testingProject1.config


    @Test
    fun provideModuleAarShouldGetAllAarFromProjectFolderWhenModulesDirIsNotProjectRoot(){
        val config = config.copy(
            projectInput = config.projectInput.copy(modulesDirIsProjectRoot = false)
        )

        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleAars = cltInputProvider.provideModuleAar().toList().sorted().toTypedArray()
        val expectingAllAars = testingProject1.expectingAllAars.sorted().toTypedArray()
        Assert.assertEquals(expectingAllAars.size, 3)
        Assert.assertArrayEquals(moduleAars, expectingAllAars)
    }

    @Test
    fun provideModuleJarShouldGetAllJarFromProjectFolderWhenModulesDirIsNotProjectRoot() {
        val config = config.copy(
            projectInput = config.projectInput.copy(modulesDirIsProjectRoot = false)
        )

        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleJars = cltInputProvider.provideModuleJar().toList().sorted().toTypedArray()
        val expectingAllJars = testingProject1.expectingAllJars.sorted().toTypedArray()
        Assert.assertEquals(expectingAllJars.size, 2)
        Assert.assertArrayEquals(moduleJars, expectingAllJars)
    }

    @Test
    fun provideModuleAarShouldGetCorrectAarFromProjectFolderWhenModulesDirIsProjectRoot() {
        val config = config.copy(
            projectInput = config.projectInput.copy(modulesDirIsProjectRoot = true)
        )

        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleAars = cltInputProvider.provideModuleAar().toList().sorted().toTypedArray()
        val expectingModuleAars = testingProject1.expectingModuleAars.sorted().toTypedArray()
        Assert.assertEquals(expectingModuleAars.size, 2)
        Assert.assertArrayEquals(moduleAars, expectingModuleAars)
    }

    @Test
    fun provideModuleJarShouldGetCorrectJarFromProjectFolderWhenModulesDirIsProjectRoot() {
        val config = config.copy(
            projectInput = config.projectInput.copy(modulesDirIsProjectRoot = true)
        )

        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val moduleJars = cltInputProvider.provideModuleJar().toList().sorted().toTypedArray()
        val expectingModuleJars = testingProject1.expectingModuleJars.sorted().toTypedArray()
        Assert.assertEquals(expectingModuleJars.size, 1)
        Assert.assertArrayEquals(moduleJars, expectingModuleJars)
    }

    @Test
    fun provideLibraryAarShouldGetAllAarFromFolder() {
        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val libraryAar = cltInputProvider.provideLibraryAar().toList().sorted().toTypedArray()
        val expectingLibAars = testingProject1.expectingLibAars.sorted().toTypedArray()
        Assert.assertEquals(expectingLibAars.size, 2)
        Assert.assertArrayEquals(libraryAar, expectingLibAars)
    }

    @Test
    fun provideLibraryJarShouldGetAllAarFromFolder() {
        val cltInputProvider = CltInputProvider(
            fileQuery = fileQuery,
            config = config,
            apksDirectory = File("FakeDir"),
            fileSystem = testingProject1
        )
        val libraryJars = cltInputProvider.provideLibraryJar().toList().sorted().toTypedArray()
        val expectingLibJars = testingProject1.expectingLibJars.sorted().toTypedArray()
        Assert.assertEquals(expectingLibJars.size, 2)
        Assert.assertArrayEquals(libraryJars, expectingLibJars)
    }

}
package com.grab.sizer.parser

import com.grab.sizer.utils.SizerInputFile
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DefaultJarFileParserTest {

    private lateinit var jarFileParser: DefaultJarFileParser

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Before
    fun setup() {
        jarFileParser = DefaultJarFileParser()
    }

    @Test
    fun parseJarsShouldCorrectlyParseClassFiles() {
        val jar = createTestJar("test.jar", listOf("com/example/Test1.class", "com/example/Test2.class"))
        val result = jarFileParser.parseJars(sequenceOf(jar))

        assertEquals(1, result.size)
        val jarInfo = result.first()
        assertEquals(2, jarInfo.classes.size)
        assertTrue(jarInfo.classes.any { it.name == "com.example.Test1" })
        assertTrue(jarInfo.classes.any { it.name == "com.example.Test2" })
    }

    @Test
    fun parseJarsShouldCorrectlyParseNativeLibraries() {
        val jar = createTestJar("test.jar", listOf("lib/x86/libtest1.so", "lib/arm64-v8a/libtest2.so"))
        val result = jarFileParser.parseJars(sequenceOf(jar))

        assertEquals(1, result.size)
        val jarInfo = result.first()
        assertEquals(2, jarInfo.nativeLibs.size)
        assertTrue(jarInfo.nativeLibs.any { it.path == "/lib/x86/libtest1.so" })
        assertTrue(jarInfo.nativeLibs.any { it.path == "/lib/arm64-v8a/libtest2.so" })
    }

    @Test
    fun parseJarsShouldCorrectlyParseOtherFiles() {
        val jar = createTestJar("test.jar", listOf("resources/test.txt", "META-INF/MANIFEST.MF"))
        val result = jarFileParser.parseJars(sequenceOf(jar))

        assertEquals(1, result.size)
        val jarInfo = result.first()
        assertEquals(2, jarInfo.others.size)
        assertTrue(jarInfo.others.any { it.path == "/resources/test.txt" })
        assertTrue(jarInfo.others.any { it.path == "/META-INF/MANIFEST.MF" })
    }

    @Test
    fun parseJarsShouldCorrectlyHandleMixedContent() {
        val jar = createTestJar(
            "test.jar", listOf(
                "com/example/Test.class",
                "lib/x86/libtest.so",
                "resources/test.txt"
            )
        )
        val result = jarFileParser.parseJars(sequenceOf(jar))

        assertEquals(1, result.size)
        val jarInfo = result.first()
        assertEquals(1, jarInfo.classes.size)
        assertEquals(1, jarInfo.nativeLibs.size)
        assertEquals(1, jarInfo.others.size)
    }

    @Test
    fun parseJarsShouldCorrectlyParseMultipleJars() {
        val jar1 = createTestJar("test1.jar", listOf("com/example/Test1.class"))
        val jar2 = createTestJar("test2.jar", listOf("lib/x86/libtest.so"))
        val result = jarFileParser.parseJars(sequenceOf(jar1, jar2))

        assertEquals(2, result.size)
        val jarInfo1 = result.find { it.name == "test1.jar" }
        val jarInfo2 = result.find { it.name == "test2.jar" }

        assertNotNull(jarInfo1)
        assertEquals(1, jarInfo1!!.classes.size)

        assertNotNull(jarInfo2)
        assertEquals(1, jarInfo2!!.nativeLibs.size)
    }

    @Test
    fun parseJarsShouldHandleEmptyJarFiles() {
        val emptyJar = createTestJar("empty.jar", listOf())
        val result = jarFileParser.parseJars(sequenceOf(emptyJar))

        assertEquals(1, result.size)
        val jarInfo = result.first()
        assertEquals("empty.jar", jarInfo.name)
        assertEquals("empty", jarInfo.tag)
        assertTrue(jarInfo.classes.isEmpty())
        assertTrue(jarInfo.nativeLibs.isEmpty())
        assertTrue(jarInfo.others.isEmpty())
    }

    private fun createTestJar(name: String, entries: List<String>): SizerInputFile {
        val jarFile = tempFolder.newFile(name)
        ZipOutputStream(jarFile.outputStream()).use { zos ->
            for (entry in entries) {
                zos.putNextEntry(ZipEntry(entry))
                zos.write("test content".toByteArray())
                zos.closeEntry()
            }
        }
        return SizerInputFile(
            file = jarFile,
            tag = jarFile.nameWithoutExtension
        )

    }
}
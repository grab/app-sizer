package com.grab.sizer.parser

import com.grab.sizer.SizeCalculationMode
import com.grab.sizer.utils.SizerInputFile
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DefaultAarFileParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockJarParser: MockJarStreamParser
    private lateinit var aarFileParser: DefaultAarFileParser

    @Before
    fun setup() {
        mockJarParser = MockJarStreamParser()
        aarFileParser = DefaultAarFileParser(mockJarParser, SizeCalculationMode.DOWNLOADABLE)
    }

    @Test
    fun parseAarsShouldCorrectlyParseResourceFiles() {
        val aar = createTestAar("test.aar", listOf("res/layout/activity_main.xml", "res/values/strings.xml"))
        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(2, aarInfo.resources.size)
        assertTrue(aarInfo.resources.any { it.path == "/res/layout/activity_main.xml" })
        assertTrue(aarInfo.resources.any { it.path == "/res/values/strings.xml" })
    }

    @Test
    fun parseAarsShouldCorrectlyParseAssetFiles() {
        val aar = createTestAar("test.aar", listOf("assets/fonts/roboto.ttf", "assets/images/logo.png"))
        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(2, aarInfo.assets.size)
        assertTrue(aarInfo.assets.any { it.path == "/assets/fonts/roboto.ttf" })
        assertTrue(aarInfo.assets.any { it.path == "/assets/images/logo.png" })
    }

    @Test
    fun parseAarsShouldCorrectlyParseNativeLibraries() {
        val aar = createTestAar("test.aar", listOf("jni/x86/libtest.so", "jni/arm64-v8a/libtest.so"))
        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(2, aarInfo.nativeLibs.size)
        assertTrue(aarInfo.nativeLibs.any { it.path == "/jni/x86/libtest.so" })
        assertTrue(aarInfo.nativeLibs.any { it.path == "/jni/arm64-v8a/libtest.so" })
    }

    @Test
    fun parseAarsShouldCorrectlyParseJarFiles() {
        val jarEntry = "libs/example.jar"
        mockJarParser.mockJarInfo =
            JarFileInfo("example.jar", "libs/example.jar", "", emptySet(), emptySet(), emptySet())

        val aar = createTestAar("test.aar", listOf(jarEntry))
        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(1, aarInfo.jars.size)
        assertEquals("example.jar", aarInfo.jars.first().name)
        assertEquals(jarEntry, mockJarParser.lastParsedEntry?.name)
    }

    @Test
    fun parseAarsShouldCorrectlyParseOtherFiles() {
        val aar = createTestAar("test.aar", listOf("META-INF/MANIFEST.MF", "proguard.txt"))
        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(2, aarInfo.others.size)
        assertTrue(aarInfo.others.any { it.path == "/META-INF/MANIFEST.MF" })
        assertTrue(aarInfo.others.any { it.path == "/proguard.txt" })
    }

    @Test
    fun parseAarsShouldCorrectlyHandleMixedContent() {
        val aar = createTestAar(
            "test.aar", listOf(
                "res/layout/activity_main.xml",
                "assets/fonts/roboto.ttf",
                "jni/x86/libtest.so",
                "libs/example.jar",
                "proguard.txt"
            )
        )

        mockJarParser.mockJarInfo =
            JarFileInfo("example.jar", "libs/example.jar", "", emptySet(), emptySet(), emptySet())

        val result = aarFileParser.parseAars(sequenceOf(aar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals(1, aarInfo.resources.size)
        assertEquals(1, aarInfo.assets.size)
        assertEquals(1, aarInfo.nativeLibs.size)
        assertEquals(1, aarInfo.jars.size)
        assertEquals(1, aarInfo.others.size)
    }

    @Test
    fun parseAarsShouldCorrectlyParseMultipleAars() {
        val aar1 = createTestAar("test1.aar", listOf("res/layout/activity_main.xml"))
        val aar2 = createTestAar("test2.aar", listOf("jni/x86/libtest.so"))
        val result = aarFileParser.parseAars(sequenceOf(aar1, aar2))

        assertEquals(2, result.size)
        val aarInfo1 = result.find { it.name == "test1.aar" }
        val aarInfo2 = result.find { it.name == "test2.aar" }

        assertNotNull(aarInfo1)
        assertEquals(1, aarInfo1!!.resources.size)

        assertNotNull(aarInfo2)
        assertEquals(1, aarInfo2!!.nativeLibs.size)
    }

    @Test
    fun parseAarsShouldHandleEmptyAarFiles() {
        val emptyAar = createTestAar("empty.aar", listOf())
        val result = aarFileParser.parseAars(sequenceOf(emptyAar))

        assertEquals(1, result.size)
        val aarInfo = result.first()
        assertEquals("empty.aar", aarInfo.name)
        assertEquals("empty", aarInfo.tag)
        assertTrue(aarInfo.resources.isEmpty())
        assertTrue(aarInfo.assets.isEmpty())
        assertTrue(aarInfo.nativeLibs.isEmpty())
        assertTrue(aarInfo.jars.isEmpty())
        assertTrue(aarInfo.others.isEmpty())
    }

    private fun createTestAar(name: String, entries: List<String>): SizerInputFile {
        val aarFile = tempFolder.newFile(name)
        ZipOutputStream(aarFile.outputStream()).use { zos ->
            for (entry in entries) {
                zos.putNextEntry(ZipEntry(entry))
                zos.write("test content".toByteArray())
                zos.closeEntry()
            }
        }
        return SizerInputFile(
            file = aarFile,
            tag = aarFile.nameWithoutExtension
        )
    }

    private class MockJarStreamParser : JarStreamParser {
        var mockJarInfo: JarFileInfo? = null
        var lastParsedEntry: ZipEntry? = null

        override fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo {
            lastParsedEntry = jarEntry
            return mockJarInfo ?: throw IllegalStateException("MockJarInfo not set")
        }
    }
}
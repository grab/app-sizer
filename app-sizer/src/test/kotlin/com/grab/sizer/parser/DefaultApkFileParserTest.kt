package com.grab.sizer.parser

import com.android.tools.apk.analyzer.ApkSizeCalculator
import com.grab.sizer.SizeCalculationMode
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.io.InputStream
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultApkFileParserTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockDexFileParser: MockDexFileParser
    private lateinit var mockApkSizeCalculator: MockApkSizeCalculator
    private lateinit var apkFileParser: DefaultApkFileParser

    @Before
    fun setup() {
        mockDexFileParser = MockDexFileParser()
        mockApkSizeCalculator = MockApkSizeCalculator()
        apkFileParser = DefaultApkFileParser(mockDexFileParser, mockApkSizeCalculator, SizeCalculationMode.DOWNLOADABLE)
    }

    @Test
    fun parseApksShouldCorrectlyParseResourceFiles() {
        val apk = createTestApk("test.apk", listOf("res/layout/activity_main.xml", "res/values/strings.xml"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(2, apkInfo.resources.size)
        assertTrue(apkInfo.resources.any { it.path == "/res/layout/activity_main.xml" })
        assertTrue(apkInfo.resources.any { it.path == "/res/values/strings.xml" })
    }

    @Test
    fun parseApksShouldCorrectlyParseAssetFiles() {
        val apk = createTestApk("test.apk", listOf("assets/fonts/roboto.ttf", "assets/images/logo.png"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(2, apkInfo.assets.size)
        assertTrue(apkInfo.assets.any { it.path == "/assets/fonts/roboto.ttf" })
        assertTrue(apkInfo.assets.any { it.path == "/assets/images/logo.png" })
    }

    @Test
    fun parseApksShouldCorrectlyParseNativeLibraries() {
        val apk = createTestApk("test.apk", listOf("lib/x86/libtest.so", "lib/arm64-v8a/libtest.so"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(2, apkInfo.nativeLibs.size)
        assertTrue(apkInfo.nativeLibs.any { it.path == "/lib/x86/libtest.so" })
        assertTrue(apkInfo.nativeLibs.any { it.path == "/lib/arm64-v8a/libtest.so" })
    }

    @Test
    fun parseApksShouldCorrectlyParseDexFiles() {
        val apk = createTestApk("test.apk", listOf("classes.dex", "classes2.dex"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()
        mockDexFileParser.setDexFileInfos(
            listOf(
                DexFileInfo("classes.dex", 1000, emptySet(), emptySet(), 500, SizeCalculationMode.DOWNLOADABLE),
                DexFileInfo("classes2.dex", 1000, emptySet(), emptySet(), 500, SizeCalculationMode.DOWNLOADABLE)
            )
        )

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(2, apkInfo.dexes.size)
        assertTrue(apkInfo.dexes.any { it.name == "classes.dex" })
        assertTrue(apkInfo.dexes.any { it.name == "classes2.dex" })
    }

    @Test
    fun parseApksShouldCorrectlyParseOtherFiles() {
        val apk = createTestApk("test.apk", listOf("META-INF/MANIFEST.MF", "resources.arsc"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(2, apkInfo.others.size)
        assertTrue(apkInfo.others.any { it.path == "/META-INF/MANIFEST.MF" })
        assertTrue(apkInfo.others.any { it.path == "/resources.arsc" })
    }

    @Test
    fun parseApksShouldCorrectlyHandleMixedContent() {
        val apk = createTestApk("test.apk", listOf(
            "res/layout/activity_main.xml",
            "assets/fonts/roboto.ttf",
            "lib/x86/libtest.so",
            "classes.dex",
            "resources.arsc"
        ))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()
        mockDexFileParser.setDexFileInfos(
            listOf(DexFileInfo("classes.dex", 1000, emptySet(), emptySet(), 500, SizeCalculationMode.DOWNLOADABLE))
        )

        val result = apkFileParser.parseApks(sequenceOf(apk), ProguardMap())

        assertEquals(1, result.size)
        val apkInfo = result.first()
        assertEquals(1, apkInfo.resources.size)
        assertEquals(1, apkInfo.assets.size)
        assertEquals(1, apkInfo.nativeLibs.size)
        assertEquals(1, apkInfo.dexes.size)
        assertEquals(1, apkInfo.others.size)
    }

    @Test
    fun parseApksShouldCorrectlyParseMultipleApks() {
        val apk1 = createTestApk("test1.apk", listOf("res/layout/activity_main.xml"))
        val apk2 = createTestApk("test2.apk", listOf("lib/x86/libtest.so"))
        mockApkSizeCalculator.mockApkSizeInfo = createMockApkSizeInfo()

        val result = apkFileParser.parseApks(sequenceOf(apk1, apk2), ProguardMap())

        assertEquals(2, result.size)
        val apkInfo1 = result.find { it.name == "test1.apk" }
        val apkInfo2 = result.find { it.name == "test2.apk" }

        assertEquals(1, apkInfo1?.resources?.size)
        assertEquals(1, apkInfo2?.nativeLibs?.size)
    }

    private fun createTestApk(name: String, entries: List<String>): File {
        val apkFile = tempFolder.newFile(name)
        ZipOutputStream(apkFile.outputStream()).use { zos ->
            for (entry in entries) {
                zos.putNextEntry(ZipEntry(entry))
                zos.write("test content".toByteArray())
                zos.closeEntry()
            }
        }
        return apkFile
    }

    private fun createMockApkSizeInfo(): ApkSizeInfo {
        return ApkSizeInfo(
            downloadSize = 1000,
            size = 2000,
            downloadFileSizeMap = mapOf("res/layout/activity_main.xml" to 100L),
            rawFileSizeMap = mapOf("res/layout/activity_main.xml" to 200L)
        )
    }

    private class MockDexFileParser : DexFileParser {
        private var mockDexFileInfo: MutableList<DexFileInfo> = mutableListOf()
        fun setDexFileInfos(values : List<DexFileInfo>){
            mockDexFileInfo = values.toMutableList()
        }

        override fun parse(entry: ZipEntry, inputStream: InputStream, apkSizeInfo: ApkSizeInfo, proguardMap: ProguardMap?): DexFileInfo {
            if(mockDexFileInfo.isNotEmpty()) return mockDexFileInfo.removeFirst()
            throw IllegalStateException("MockDexFileInfo not set")
        }
    }

    private class MockApkSizeCalculator : ApkSizeCalculator {
        var mockApkSizeInfo: ApkSizeInfo? = null

        override fun getFullApkDownloadSize(apk: Path): Long = mockApkSizeInfo?.downloadSize ?: 0

        override fun getFullApkRawSize(apk: Path): Long = mockApkSizeInfo?.size ?: 0

        override fun getDownloadSizePerFile(apk: Path): Map<String, Long> = mockApkSizeInfo?.downloadFileSizeMap ?: emptyMap()

        override fun getRawSizePerFile(apk: Path): Map<String, Long> = mockApkSizeInfo?.rawFileSizeMap ?: emptyMap()
    }
}
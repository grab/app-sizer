package com.grab.tools.apk

import com.grab.tools.RawFileInfo
import org.xmlpull.v1.XmlPullParserFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class ManifestFileParserTest {

    @Test
    fun test_ManifestFileParserImpl_parse() {
        val stream = javaClass.getResource("/AndroidManifest.xml").openStream()
        val xmlPullParserFactory = XmlPullParserFactory.newInstance()
        val parser = ManifestFileParserImpl(xmlPullParserFactory)
        val manifestFileInfo = parser.parse(stream, RawFileInfo(
            "/AndroidManifest.xml", 1000L, 100L, 1000L))

        assertEquals(manifestFileInfo.versionCode, "1")
        assertEquals(manifestFileInfo.versionName, "1.0")
    }
}
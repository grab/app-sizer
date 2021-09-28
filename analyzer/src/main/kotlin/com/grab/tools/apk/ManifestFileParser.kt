package com.grab.tools.apk

import com.grab.tools.RawFileInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.io.InputStreamReader

const val TAG_MANIFEST = "manifest"
const val PROP_VERSION_CODE = "versionCode"
const val PROP_VERSION_NAME = "versionName"
const val ATTR_VERSION_CODE = "android:$PROP_VERSION_CODE"
const val ATTR_VERSION_NAME = "android:$PROP_VERSION_NAME"

interface ManifestFileParser {

    fun parse(manifestStream: InputStream, rawFileInfo: RawFileInfo): ManifestFileInfo
}

class ManifestFileParserImpl(
    private val xmlPullParserFactory: XmlPullParserFactory
) : ManifestFileParser {

    override fun parse(manifestStream: InputStream, rawFileInfo: RawFileInfo): ManifestFileInfo {
        val infoMap: MutableMap<String, Any?> = mutableMapOf()

        manifestStream.use {
            val xmlPullParser = xmlPullParserFactory.newPullParser()
            xmlPullParser.setInput(InputStreamReader(it))

            var eventType = xmlPullParser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xmlPullParser.name == TAG_MANIFEST) {
                    for (i in 0 until xmlPullParser.attributeCount) {
                        when (xmlPullParser.getAttributeName(i)) {
                            ATTR_VERSION_CODE -> infoMap[PROP_VERSION_CODE] = xmlPullParser.getAttributeValue(i)
                            ATTR_VERSION_NAME -> infoMap[PROP_VERSION_NAME] = xmlPullParser.getAttributeValue(i)
                        }
                    }
                }
                eventType = xmlPullParser.next()
            }

            it.close()
        }

        return rawFileInfo.toManifestFileInfo(infoMap)
    }

    private fun RawFileInfo.toManifestFileInfo(infoMap: MutableMap<String, Any?>) = ManifestFileInfo(
        path = path,
        versionCode = infoMap[PROP_VERSION_CODE] as? String,
        versionName = infoMap[PROP_VERSION_NAME] as? String?,
        downloadSize = downloadSize,
        compressedSize = compressedSize,
        size = size
    )
}
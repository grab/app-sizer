package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.ClassFileInfo
import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject


/**
 * JarStreamParser interface provides a method to parse a JAR file within an AAR.
 * It uses the ZipEntry of the JAR file and a provided InputStream to access JAR content within the AAR file.
 */
interface JarStreamParser {
    /**
     * Parses the contents of a JAR file within the AAR file.
     * @param jarEntry ZipEntry of the JAR file within the AAR file.
     * @param inputStream InputStream to access the JAR content.
     * @return A JarFileInfo object containing the properties of parsed JAR file.
     */
    fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo
}

@AppScope
class DefaultJarStreamParser @Inject constructor() : JarStreamParser {
    override fun parse(jarEntry: ZipEntry, inputStream: InputStream): JarFileInfo {
        ZipInputStream(inputStream).use { entries ->
            val others = mutableSetOf<RawFileInfo>()
            val classes = mutableSetOf<ClassFileInfo>()
            var entry = entries.nextEntry
            while (entry != null) {
                val fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    size = entry.size,
                    downloadSize = -1
                )
                when (fileInfo.type) {
                    FileType.CLASS -> classes.add(entry.toClass())
                    else -> others.add(fileInfo)
                }
                entry = entries.nextEntry
            }
            return JarFileInfo(
                name = jarEntry.getPath(),
                path = "",
                others = others,
                nativeLibs = emptySet(),
                classes = classes
            )
        }
    }
}

internal fun ZipEntry.toClass(): ClassFileInfo {
    return ClassFileInfo(
        /**
         * Convert ZipEntry name to class name
         * Example: "/com/grab/sample/dummy/DummyClass1.class" -> "com.grab.sample.dummy.DummyClass1"
         */
        name = name.replace('/', '.').removeSuffix(".class"),
        size = size
    )
}



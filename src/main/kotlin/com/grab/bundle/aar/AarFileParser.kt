package com.grab.bundle.aar

import com.grab.bundle.FileQuery
import com.grab.bundle.FileType
import com.grab.bundle.RawFileInfo
import com.grab.bundle.jar.JarFileInfo
import com.grab.bundle.jar.JarStreamParser
import com.grab.bundle.jar.JarStreamParserImpl
import java.io.File
import java.util.zip.ZipFile

interface AarFileParser {
    fun parse(file: File): AarFileInfo
    fun parseAars(dir: File): Set<AarFileInfo>
}

// http://tools.android.com/tech-docs/new-build-system/aar-format
class AarFileParserImpl(private val fileQuery: FileQuery, private val jarParser: JarStreamParser) : AarFileParser {

    override fun parse(file: File): AarFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            var jars = mutableSetOf<JarFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                var fileInfo = RawFileInfo(entry.name, entry.compressedSize, entry.size)
                // Todo, this is a hack
                if (fileInfo.type == FileType.NATIVE_LIB) {
                    fileInfo = fileInfo.copy(path = fileInfo.path.replace("jni", "lib"))
                }
                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.JAR -> {
                        if (!fileInfo.path.startsWith("lint.jar"))
                            jars.add(jarParser.parse(entry, zipFile.getInputStream(entry)))
                    }
                    FileType.OTHERS -> others.add(fileInfo)
                }
            }
            return AarFileInfo(
                name = file.name,
                path = file.path,
                resources = resources,
                assets = assets,
                nativeLibs = nativeLibs,
                others = others,
                jars = jars
            )
        }
    }

    override fun parseAars(dir: File): Set<AarFileInfo> {
        return fileQuery.searchFile(dir, "aar")
            .map { file -> parse(file) }
            .toSet()
    }
}

fun main() {
    val fileQuery = FileQuery()
    val jarFileParser = JarStreamParserImpl()
    AarFileParserImpl(fileQuery, jarFileParser).parse(
        File("/Users/van.minh/Desktop/temp/activity-1.2.3.aar")
    )
}



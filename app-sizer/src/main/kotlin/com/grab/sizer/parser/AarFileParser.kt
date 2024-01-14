package com.grab.sizer.parser

import com.grab.sizer.analyzer.model.FileType
import com.grab.sizer.analyzer.model.RawFileInfo
import com.grab.sizer.di.AppScope
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject

interface AarFileParser {
    fun parseAars(files : Sequence<File>): Set<AarFileInfo>
}

// http://tools.android.com/tech-docs/new-build-system/aar-format
@AppScope
class DefaultAarFileParser @Inject constructor(private val jarParser: JarStreamParser) : AarFileParser {

    private fun parse(file: File): AarFileInfo {
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()
            val resources = mutableSetOf<RawFileInfo>()
            val assets = mutableSetOf<RawFileInfo>()
            val nativeLibs = mutableSetOf<RawFileInfo>()
            val others = mutableSetOf<RawFileInfo>()
            var jars = mutableSetOf<JarFileInfo>()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                var fileInfo = RawFileInfo(
                    path = entry.getPath(),
                    compressedSize = entry.compressedSize,
                    size = entry.size,
                    downloadSize = -1,
                )
                // Todo, this is a hack
                if (fileInfo.type == FileType.NATIVE_LIB) {
                    fileInfo = fileInfo.copy(path = fileInfo.path.replace("jni", "lib"))
                }
                when (fileInfo.type) {
                    FileType.RESOURCE -> resources.add(fileInfo)
                    FileType.ASSET -> assets.add(fileInfo)
                    FileType.NATIVE_LIB -> nativeLibs.add(fileInfo)
                    FileType.JAR -> {
                        jars.add(jarParser.parse(entry, zipFile.getInputStream(entry)))
                    }
                    else -> others.add(fileInfo)
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

    override fun parseAars(files : Sequence<File>): Set<AarFileInfo> {
        return files.map { file -> parse(file) }.toSet()
    }
}



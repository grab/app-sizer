package com.grab.bundle.apk

import com.grab.bundle.ClassFileInfo
import com.grab.bundle.FileQuery
import com.grab.bundle.log.log
import org.jf.dexlib2.dexbacked.DexBackedClassDef
import org.jf.dexlib2.dexbacked.DexBackedDexFile
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.BufferedInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry

interface DexFileParser {
    fun parse(entry: ZipEntry, inputStream: InputStream, proguardMap: ProguardMap? = null): DexFileInfo
}

class DexFileParserImpl() : DexFileParser {
    override fun parse(entry: ZipEntry, inputStream: InputStream, proguardMap: ProguardMap?): DexFileInfo {
        val dexBackedDexFile = DexBackedDexFile.fromInputStream(null, BufferedInputStream(inputStream))

        val classes = dexBackedDexFile.classes
            .map { classDef -> fromDex(classDef, proguardMap) }
            .toSet()

        return DexFileInfo(
            name = entry.name,
            compressedSize = entry.compressedSize,
            size = entry.size,
            classes = classes,
        )
    }

    private fun fromDex(classDef: DexBackedClassDef, proguardMap: ProguardMap?): ClassFileInfo {
        val className = classDef.type.removePrefix("L").replace('/', '.').removeSuffix(";")
        if(proguardMap!= null && proguardMap.getClassName(className) == null){
            log("Can not find $className in from proguard mapping file")
        }
        return ClassFileInfo(
            name = proguardMap?.getClassName(className) ?: className,
            size = classDef.size.toLong()
        )
    }
}

fun main() {
    val fileQuery = FileQuery()
    val proguardMapParser = ProguardMappingParser()
    val map = proguardMapParser.parse(File("/Users/van.minh/Downloads/mapping.txt"))

    val dexParser = DexFileParserImpl()
    ApkFileParserImpl(fileQuery, dexParser).parse(
        File("/Users/van.minh/Downloads/51650000/base.apk"),
        map
    )
}
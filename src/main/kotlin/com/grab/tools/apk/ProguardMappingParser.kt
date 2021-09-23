package com.grab.tools.apk

import com.google.common.base.Charsets
import com.grab.tools.log.log
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.text.ParseException

class ProguardMappingParser() {
    fun parse(mappingFile: File): ProguardMap = ProguardMap().apply {
        try {
            readFromReader(InputStreamReader(Files.newInputStream(mappingFile.toPath()), Charsets.UTF_8))
        } catch (e: IOException) {
            log(e)
        } catch (e: ParseException) {
            log(e)
        }
    }
}
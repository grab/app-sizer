package com.grab.tools.apk

import com.google.common.base.Charsets
import com.grab.tools.log.log
import com.grab.tools.utils.InputFileProvider
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.text.ParseException
import javax.inject.Inject

class ProguardMappingProvider @Inject constructor(
    private val inputFileProvider: InputFileProvider
) {
    fun provide(): ProguardMap = ProguardMap().apply {
        val r8MappingFile = inputFileProvider.provideR8MappingFile() ?: return@apply
        try {
            Files.newInputStream(r8MappingFile.toPath()).use {
                readFromReader(InputStreamReader(it, Charsets.UTF_8))
            }
        } catch (e: IOException) {
            log(e)
        } catch (e: ParseException) {
            log(e)
        }
    }
}
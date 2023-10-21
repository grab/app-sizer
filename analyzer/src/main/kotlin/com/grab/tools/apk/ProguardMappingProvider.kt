package com.grab.tools.apk

import com.google.common.base.Charsets
import com.grab.tools.utils.InputFileProvider
import com.grab.tools.utils.Logger
import com.grab.tools.utils.log
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.text.ParseException
import javax.inject.Inject

class ProguardMappingProvider @Inject constructor(
    private val inputFileProvider: InputFileProvider,
    private val logger: Logger
) {
    fun provide(): ProguardMap = ProguardMap().apply {
        val r8MappingFile = inputFileProvider.provideR8MappingFile() ?: return@apply
        try {
            Files.newInputStream(r8MappingFile.toPath()).use {
                readFromReader(InputStreamReader(it, Charsets.UTF_8))
            }
        } catch (e: IOException) {
            logger.log(e)
        } catch (e: ParseException) {
            logger.log(e)
        }
    }
}
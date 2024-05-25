package com.grab.sizer.parser

import com.google.common.base.Charsets
import com.grab.sizer.utils.Logger
import com.grab.sizer.utils.log
import shadow.bundletool.com.android.tools.proguard.ProguardMap
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Files
import java.text.ParseException
import javax.inject.Inject


/**
 * ProguardFileParser interface provides a method to generate a ProguardMap from a Proguard file.
 * If the provided file is null, an empty ProguardMap is returned.
 * Note: The ProguardMap is part of the bundletool library ("com.android.tools.build:bundletool").
 */
interface ProguardFileParser {
    fun parse(proguardFile: File?): ProguardMap
}

class DefaultProguardFileParser @Inject constructor(
    private val logger: Logger
) : ProguardFileParser {
    override fun parse(proguardFile: File?): ProguardMap = ProguardMap().apply {
        if (proguardFile == null) return@apply
        try {
            Files.newInputStream(proguardFile.toPath()).use {
                readFromReader(InputStreamReader(it, Charsets.UTF_8))
            }
        } catch (e: IOException) {
            logger.log(e)
        } catch (e: ParseException) {
            logger.log(e)
        }
    }
}
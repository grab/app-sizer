package com.grab.tools

import com.grab.tools.log.log

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    AnalyzerCommand().main(args)
    val endTime = System.currentTimeMillis()
    val delta = (endTime - startTime)/1000
    log("Total execution time $delta")
}

//@AnalyzerInputFile(INPUT_FILE_OUTPUT_FILE)
//private val output: File
//@AnalyzerInputFile(INPUT_FILE_PROGUARD_MAPPING_FILE)
//private val proguardMappingFile: File
//private const val APP_MODULE = "app"

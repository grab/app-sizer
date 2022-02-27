package com.grab.tools

import com.grab.tools.log.log

fun main(args: Array<String>) {
    val startTime = System.currentTimeMillis()
    AnalyzerCommand().main(args)
    val endTime = System.currentTimeMillis()
    val delta = (endTime - startTime)/1000
    log("Total execution time $delta")
}

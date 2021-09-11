package com.grab.bundle

import java.io.File

internal object BundleFileAnalyticFactory {
    fun create(): BundleFileAnalytic {
        return BundleFileAnalytic(bundleParser = BundleParserImpl())
    }
}


internal class BundleFileAnalytic(private val bundleParser: BundleParser) {
    fun process(bundleFile: File, libDir: File) {
        bundleParser.parse(bundleFile).get(FileType.NATIVE_LIB)?.forEach {
            println(it.path)
        }
    }
}
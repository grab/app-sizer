package com.grab.bundle

import com.grab.bundle.analytic.Analytic
import com.grab.bundle.analytic.AssetsAnalytic
import com.grab.bundle.analytic.NativeLibAnalytic
import com.grab.bundle.analytic.ResourceAnalytic
import com.grab.bundle.apk.ApkFileParser
import java.io.File

typealias Contributors = Map<String, Set<RawFileInfo>>

object ApkComponentFactory {
    fun provideFileQuery(): FileQuery = FileQuery()
    fun provideApkParser(fileQuery: FileQuery) = ApkFileParser(fileQuery)
    fun provideAarFileParser(fileQuery: FileQuery) = AarFileParser(fileQuery)
    fun provideAnalytics(): Map<String, Analytic> = mapOf(
        ResourceAnalytic.TAG to ResourceAnalytic(),
        NativeLibAnalytic.TAG to NativeLibAnalytic(),
        AssetsAnalytic.TAG to AssetsAnalytic()
    )

    fun provideApkComponentAnalytic(
        apkFileParser: ApkFileParser,
        aarFileParser: AarFileParser,
        analytics: Map<String, Analytic>
    ): ApkComponentAnalytic {
        return ApkComponentAnalytic(apkFileParser, aarFileParser, analytics)
    }
}

class ApkComponentAnalytic(
    private val apkFileParser: ApkFileParser,
    private val aarFileParser: AarFileParser,
    private val analytics: Map<String, Analytic>
) {

    fun process(apkDir: File, aarDir: File): Map<String, Contributors> {
        val apkInfo = apkFileParser.parseApks(apkDir)
        val aarInfo = aarFileParser.parseAars(aarDir)
        return analytics.mapValues { it.value.analytic(apkInfo, aarInfo) }
    }
}



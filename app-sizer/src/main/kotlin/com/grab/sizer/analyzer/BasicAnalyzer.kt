package com.grab.sizer.analyzer

import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.*
import javax.inject.Inject

/**
 * A specialized implementation of the Analyzer interface that focuses on basic APK analysis.
 * This class specifically handles [com.grab.sizer.AnalyticsOption.BASIC] and provides metrics similar to those
 * obtained by opening the APK file in Android Studio, including:
 * - apk : The total download size of the app.
 * - resource : The cumulative size contribution from resources such as images and layouts.
 * - native_lib : The cumulative size contribution from native libraries.
 * - asset : The cumulative size contribution from assets.
 * - code : The cumulative size contribution from Java/Kotlin code.
 *
 * @property projectInfoProvider Provides project-related information.
 * @property dataParser Parses APK, AAR and JAR for analysis.
 **/
internal class BasicApkAnalyzer @Inject constructor(
    private val projectInfoProvider: ProjectInfoProvider,
    private val dataParser: DataParser
) : Analyzer {
    override fun process(): Report {
        val androidBinaryInfo = dataParser.apks
        return Report(
            rows = androidBinaryInfo.createApkReportRows(androidBinaryInfo.dexDownloadRatio()),
            id = METRICS_ID_BASIC,
            name = METRICS_ID_BASIC,
            projectInfo = projectInfoProvider.getProjectInfo(),
            customProperties = projectInfoProvider.getCustomProperties()
        )
    }

    private fun Set<ApkFileInfo>.createApkReportRows(dexCompressedRatio: Double): List<Row> {
        val resourceDownloadSize = flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibDownloadSize = flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetDownloadSize = flatMap { it.assets }.sumOf { it.downloadSize }
        val otherDownloadSize = flatMap { it.others }.sumOf { it.downloadSize }
        val dexDownloadFile = flatMap { it.dexes }.sumOf { it.downloadSize }

        val classesSize = flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
        val total =
            resourceDownloadSize + nativeLibDownloadSize + assetDownloadSize + otherDownloadSize + classDownloadSize

        return listOf(
            createRow(
                name = "apk",
                value = total
            ),
            createRow(
                name = "resource",
                value = resourceDownloadSize
            ),
            createRow(
                name = "native_lib",
                value = nativeLibDownloadSize
            ),
            createRow(
                name = "asset",
                value = assetDownloadSize
            ),
            createRow(
                name = "other",
                value = otherDownloadSize
            ),
            createRow(
                name = "code",
                value = dexDownloadFile
            )
        )
    }
}

internal fun createRow(name: String, value: Long): Row = Row(
    fields = createContributorFields(name, value),
    name = name
)

internal fun createContributorFields(name: String, value: Long): List<Field> = listOf(
    TagField(
        name = KEY_CONTRIBUTOR,
        value = name
    ),
    DefaultField(
        name = KEY_SIZE,
        value = value
    )
)
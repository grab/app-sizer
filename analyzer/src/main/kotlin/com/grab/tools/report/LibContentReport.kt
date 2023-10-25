package com.grab.tools.report

import com.grab.tools.AnalyticsOption
import com.grab.tools.analyzer.report.*
import com.grab.tools.apk.ApkFileInfo
import com.grab.tools.model.Contributor
import com.grab.tools.model.FileInfo
import java.io.File
import javax.inject.Inject
import javax.inject.Named


class LibContentReport @Inject constructor(
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider,
    @Named(NAMED_LIB_NAME)
    private val libName: String?,
) : AnalyticReport {
    override fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val library = contributors.find { File(it.path).nameWithoutExtension == libName }
            ?: throw RuntimeException("Can not find the $libName")
        val resourceRows = library.resources.toReportRows("Resource")
        val assetRows = library.assets.toReportRows("Asset")
        val nativeLibRows = library.nativeLibs.toReportRows("Native")
        val otherRows = library.others.toReportRows("Other")
        // todo : calculating the download size from the analytic process
        val classRows = library.classes
            .map { clazz -> clazz.copy(downloadSize = (clazz.size * dexCompressedRatio).toLong()) }
            .toReportRows("Class")

        reportWriters.forEach {
            it.write(
                AnalyticsOption.LIB_CONTENT.name.toLowerCase(),
                Report(
                    projectInfo = projectInfoProvider.get(),
                    id = LIB_CONTENT_METRICS_ID,
                    name = LIB_CONTENT_METRICS_ID,
                    rows = resourceRows + assetRows + nativeLibRows + otherRows + classRows
                )
            )
        }
    }

    private fun Collection<FileInfo>.toReportRows(type: String): List<Row> = map {
        Row(
            name = it.name,
            fields = listOf(
                Field.createDefault("file-name", it.name),
                Field.createDefault("size", it.downloadSize),
                TagField("type", type),
            )
        )
    }

}


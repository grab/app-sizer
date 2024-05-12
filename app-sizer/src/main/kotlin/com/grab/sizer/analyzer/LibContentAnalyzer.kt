package com.grab.sizer.analyzer

import com.grab.sizer.AnalyticsOption
import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.di.NAMED_LIB_NAME
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.*
import com.grab.sizer.report.dexDownloadRatio
import java.io.File
import javax.inject.Inject
import javax.inject.Named

internal class LibContentAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val reportWriters: Set<@JvmSuppressWildcards ReportWriter>,
    private val projectInfoProvider: ProjectInfoProvider,
    private val dataParser: DataParser,
    @Named(NAMED_LIB_NAME)
    private val libName: String?
) : Analyzer {
    override fun process() {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        report(dataParser.apks, processedData.contributors)
    }

    private fun report(apks: Set<ApkFileInfo>, contributors: Set<Contributor>) {
        val dexCompressedRatio = apks.dexDownloadRatio()
        val library = contributors.find { File(it.path).nameWithoutExtension == libName }
            ?: throw RuntimeException("Can not find the $libName")
        val resourceRows = library.resources.toReportRows("Resource")
        val assetRows = library.assets.toReportRows("Asset")
        val nativeLibRows = library.nativeLibs.toReportRows("Native")
        val otherRows = library.others.toReportRows("Other")
        // todo : calculating the download sizer from the analytic process
        val classRows = library.classes
            .map { clazz -> clazz.copy(downloadSize = (clazz.size * dexCompressedRatio).toLong()) }
            .toReportRows("Class")

        reportWriters.forEach {
            it.write(
                AnalyticsOption.LIB_CONTENT.name.toLowerCase(),
                Report(
                    id = LIB_CONTENT_METRICS_ID,
                    name = LIB_CONTENT_METRICS_ID,
                    rows = resourceRows + assetRows + nativeLibRows + otherRows + classRows,
                    projectInfo = projectInfoProvider.getProjectInfo(),
                    customProperties = projectInfoProvider.getCustomProperties()
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
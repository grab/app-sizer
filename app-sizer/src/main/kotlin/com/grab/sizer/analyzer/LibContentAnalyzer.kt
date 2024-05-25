package com.grab.sizer.analyzer

import com.grab.sizer.analyzer.mapper.ApkComponentProcessor
import com.grab.sizer.analyzer.model.Contributor
import com.grab.sizer.analyzer.model.FileInfo
import com.grab.sizer.di.NAMED_LIB_NAME
import com.grab.sizer.parser.ApkFileInfo
import com.grab.sizer.parser.DataParser
import com.grab.sizer.report.*
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * A specific implementation of the Analyzer interface with a focus on analysis a library content.
 * This class handles [com.grab.sizer.AnalyticsOption.LIB_CONTENT] and generates a detail report on the library content
 *
 * @property apkComponentProcessor Responsible for processing APK, AAR, or JAR files to compile a list of contributors.
 * @property dataParser Parse APK, AAR, or JAR files.
 * @property projectInfoProvider Provide project-related information.
 */
internal class LibContentAnalyzer @Inject constructor(
    private val apkComponentProcessor: ApkComponentProcessor,
    private val projectInfoProvider: ProjectInfoProvider,
    private val dataParser: DataParser,
    @Named(NAMED_LIB_NAME)
    private val libName: String?
) : Analyzer {
    override fun process(): Report {
        val processedData = apkComponentProcessor.process(
            dataParser.apks,
            dataParser.libAars,
            dataParser.libJars
        )
        return generateReport(dataParser.apks, processedData.contributors)
    }

    private fun generateReport(apks: Set<ApkFileInfo>, contributors: Set<Contributor>): Report {
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
        return Report(
            id = LIB_CONTENT_METRICS_ID,
            name = LIB_CONTENT_METRICS_ID,
            rows = resourceRows + assetRows + nativeLibRows + otherRows + classRows,
            projectInfo = projectInfoProvider.getProjectInfo(),
            customProperties = projectInfoProvider.getCustomProperties()
        )
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
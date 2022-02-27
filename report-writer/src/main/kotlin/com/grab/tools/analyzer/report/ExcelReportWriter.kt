package com.grab.tools.analyzer.report

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

private const val KILO_BYTE = 1024L
private const val MEGA_BYTE = 1024L * 1024L

class ExcelReportWriter(
    private val outputFile: File
) : ReportWriter {
    override fun write(appInfo: AppInfo, reports: List<ReportItem>, reportId: String) {
        val workbook = WorkbookFactory.create(false)
        val sheet = workbook.createSheet("Apk Analyzer Report")
        createHeader(sheet)
        reports.forEachIndexed { i, report ->
            sheet.createRow(i + 1).apply {
                listOf(
                    report.name,
                    report.totalDownloadSize.reportSize(),
                    report.classesDownloadSize.reportSize(),
                    report.classesSize.reportSize(),
                    report.nativeLibDownloadSize.reportSize(),
                    report.resourceDownloadSize.reportSize(),
                    report.assetDownloadSize.reportSize(),
                    report.otherDownloadSize.reportSize(),
                    report.extraInfo,
                    report.owner
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }
        }
        outputFile.initOutPutFile()
        workbook.write(outputFile.outputStream())
    }

    private fun File.initOutPutFile() {
        if (!exists()) {
            if (!parentFile.exists())
                parentFile.mkdirs()
            createNewFile()
        }
    }

    private fun createHeader(sheet: Sheet) {
        sheet.createRow(0).apply {
            listOf(
                "Components",
                "Total",
                "Classes",
                "Classes (extracted)",
                "Native libs",
                "Resource",
                "Assets",
                "Others",
                "Extra information"
            ).forEachIndexed { i, text ->
                createCell(i).apply { setCellValue(text) }
            }
        }
    }
}

internal fun Long.reportSize(): String = when {
    this < KILO_BYTE -> "$this bytes"
    this < MEGA_BYTE -> "%.3f KB".format(this.toDouble() / KILO_BYTE)
    else -> "%.3f MB".format(this.toDouble() / MEGA_BYTE)
}
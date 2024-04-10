package com.grab.sizer.report

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.util.*

private const val KILO_BYTE = 1024L
private const val MEGA_BYTE = 1024L * 1024L

class XlsReportWriter(
    private val outputDirectory: File
) : ReportWriter {
    override fun write(reportId: String, report: Report) {
        val workbook = WorkbookFactory.create(false).apply {
            createSheet("App Sizer Report").apply {
                createHeader(this, report.rows)
                createRows(report.rows, this)
            }
        }

        File(File(outputDirectory, report.projectInfo.deviceName), "$reportId-report.xls").apply {
            initOutPutFile()
            workbook.write(outputStream())
            workbook.close()
        }
    }

    private fun createRows(
        rows: List<Row>,
        sheet: Sheet
    ) {
        rows.forEachIndexed { i, row ->
            sheet.createRow(i + 1).apply {
                row.fields.forEachIndexed { index, field ->
                    createCell(index).apply {
                        when (field.value) {
                            is Long -> setCellValue((field.value as Long).reportSize())
                            else -> setCellValue(field.value.toString())
                        }
                    }
                }
            }
        }
    }

    private fun createHeader(
        sheet: Sheet,
        rows: List<Row>
    ) {
        sheet.createRow(0).also { sheetRow ->
            rows.firstOrNull()?.apply {
                this.fields.map { field ->
                    field.name.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                    }
                }.forEachIndexed { i, text ->
                    sheetRow.createCell(i).apply { setCellValue(text) }
                }
            }
        }
    }

    private fun File.initOutPutFile() {
        if (!exists()) {
            if (!parentFile.exists())
                parentFile.mkdirs()
            createNewFile()
        }
    }
}

internal fun Long.reportSize(): String = when {
    this < KILO_BYTE -> "$this bytes"
    this < MEGA_BYTE -> "%.3f KB".format(this.toDouble() / KILO_BYTE)
    else -> "%.3f MB".format(this.toDouble() / MEGA_BYTE)
}
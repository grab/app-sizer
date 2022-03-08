package com.grab.tools.analyzer.report

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

private const val KILO_BYTE = 1024L
private const val MEGA_BYTE = 1024L * 1024L

class XlsReportWriter(
    private val outputFile: File
) : ReportWriter {
    override fun write(report: Report) {
        val workbook = WorkbookFactory.create(false)
        val sheet = workbook.createSheet("Apk Analyzer Report")
        createHeader(sheet, report.rows)
        createRows(report.rows, sheet)
        outputFile.initOutPutFile()
        workbook.write(outputFile.outputStream())
    }

    private fun createRows(
        rows: List<Row>,
        sheet: Sheet
    ) {
        rows.forEachIndexed { i, row ->
            sheet.createRow(i + 1).apply {
                createCell(0).apply { setCellValue(row.name) }
                row.fields.forEachIndexed { index, field ->
                    createCell(index + 1).apply {
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
            sheetRow.createCell(0)
            rows.firstOrNull()?.apply {
                this.fields.map { field -> field.name }
                    .forEachIndexed { i, text ->
                        sheetRow.createCell(i + 1).apply { setCellValue(text) }
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
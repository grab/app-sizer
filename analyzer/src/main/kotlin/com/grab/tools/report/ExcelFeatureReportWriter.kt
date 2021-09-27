package com.grab.tools.report

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File

class ExcelFeatureReportWriter(
    private val outputFile: File
) : FeatureReportWriter {
    private lateinit var workbook: Workbook
    private lateinit var sheet: Sheet
    override fun initTile() {
        workbook = WorkbookFactory.create(false)
        sheet = workbook.createSheet("Feature Contributor Report")

        sheet.createRow(0).apply {
            listOf(
                "Library Name",
                "Total",
                "Classes",
                "Classes (extracted)",
                "Native libs",
                "Resource",
                "Assets",
                "Others",
                "Full path"
            ).forEachIndexed { i, text ->
                createCell(i).apply { setCellValue(text) }
            }
        }
    }

    override fun reportApksSize(
        total: Long,
        classDownloadSize: Long,
        classesSize: Long,
        nativeLibSize: Long,
        resourceSize: Long,
        assetSizes: Long,
        othersSize: Long
    ) {
        sheet.createRow(1).apply {
            listOf(
                "Apks",
                total.reportSize(),
                classDownloadSize.reportSize(),
                classesSize.reportSize(),
                nativeLibSize.reportSize(),
                resourceSize.reportSize(),
                assetSizes.reportSize(),
                othersSize.reportSize()
            ).forEachIndexed { index, s ->
                createCell(index).apply { setCellValue(s) }
            }
        }
    }

    override fun reportTotalFeatures(dexCompressedRatio: Double, allFeatures: Feature) {
        sheet.createRow(2).apply {
            listOf(
                "All libs",
                allFeatures.getDownloadSize(dexCompressedRatio).reportSize(),
                allFeatures.getClassDownloadSize(dexCompressedRatio).reportSize(),
                allFeatures.classSize.reportSize(),
                allFeatures.nativeLibDownloadSize.reportSize(),
                allFeatures.resourcesDownloadSize.reportSize(),
                allFeatures.assetsDownloadSize.reportSize(),
                allFeatures.othersDownloadSize.reportSize()
            ).forEachIndexed { index, s ->
                createCell(index).apply { setCellValue(s) }
            }
        }
    }

    override fun reportEachFeature(dexCompressedRatio: Double, data: List<Feature>) {
        data.forEachIndexed { row, item ->
            sheet.createRow(row + 3).apply {
                listOf(
                    item.name,
                    item.getDownloadSize(dexCompressedRatio).reportSize(),
                    item.getClassDownloadSize(dexCompressedRatio).reportSize(),
                    item.classSize.reportSize(),
                    item.nativeLibDownloadSize.reportSize(),
                    item.resourcesDownloadSize.reportSize(),
                    item.assetsDownloadSize.reportSize(),
                    item.othersDownloadSize.reportSize(),
                    item.name
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }
        }
    }

    override fun save() {
        outputFile.outputStream().use {
            workbook.write(it)
        }
    }
}
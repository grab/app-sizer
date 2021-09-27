package com.grab.tools.report

import com.grab.tools.Contributor
import com.grab.tools.RawFileInfo
import com.grab.tools.apk.ApkFileInfo
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.util.*

class ModuleAnalyticReport(
    private val outPutFile: File
) : AnalyticReport {
    override fun report(apks: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        val workbook = WorkbookFactory.create(false)
        val sheet = workbook.createSheet("Modules contributors")
        createHeader(sheet)
        val dexCompressedRatio = dexDownloadRatio(apks)
        apksSizeReport(dexCompressedRatio, apks, sheet)
        val data = sortData(dexCompressedRatio, contributor)
        totalLibsContributor(dexCompressedRatio, data, sheet)
        reportEachLib(dexCompressedRatio, data, sheet)

        outPutFile.outputStream().use {
            workbook.write(it)
        }
    }

    private fun createHeader(sheet: Sheet) {
        sheet.createRow(0).apply {
            listOf(
                "Module name",
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

    private fun dexDownloadRatio(apks: Set<ApkFileInfo>): Double {
        val dexDownloadSize = apks.flatMap { it.dexes }.sumOf { it.downloadSize }
        val dexClassesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        return dexDownloadSize.toDouble() / dexClassesSize
    }

    private fun apksSizeReport(dexCompressedRatio: Double, apks: Set<ApkFileInfo>, sheet: Sheet) {
        val resourceSize = apks.flatMap { it.resources }.sumOf { it.downloadSize }
        val nativeLibSize = apks.flatMap { it.nativeLibs }.sumOf { it.downloadSize }
        val assetSizes = apks.flatMap { it.assets }.sumOf { it.downloadSize }
        val othersSize = apks.flatMap { it.others }.sumOf { it.downloadSize }
        val classesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val classDownloadSize = (classesSize * dexCompressedRatio).toLong()
        val total = resourceSize + nativeLibSize + assetSizes + othersSize + classDownloadSize


        val others = apks.flatMap { it.others }.toList()
        Collections.sort(others, Comparator<RawFileInfo> { a, b ->
            if (a.downloadSize > b.downloadSize) -1
            else if (a.downloadSize < b.downloadSize) 1
            else 0
        })

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

    private fun totalLibsContributor(dexCompressedRatio: Double, data: List<Contributor>, sheet: Sheet) {
        data.reduce { pre, cur ->
            pre.copy(
                resources = pre.resources + cur.resources,
                assets = pre.assets + cur.assets,
                nativeLibs = pre.nativeLibs + cur.nativeLibs,
                classes = pre.classes + cur.classes,
                others = pre.others + cur.others
            )
        }.also { allLibs ->
            sheet.createRow(2).apply {
                listOf(
                    "All modules",
                    allLibs.getDownloadSize(dexCompressedRatio).reportSize(),
                    allLibs.getClassDownloadSize(dexCompressedRatio).reportSize(),
                    allLibs.classSize.reportSize(),
                    allLibs.nativeLibDownloadSize.reportSize(),
                    allLibs.resourcesDownloadSize.reportSize(),
                    allLibs.assetsDownloadSize.reportSize(),
                    allLibs.othersDownloadSize.reportSize()
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }
        }
    }

    private fun reportEachLib(dexCompressedRatio: Double, data: List<Contributor>, sheet: Sheet) {
        data.forEachIndexed { row, item ->
            sheet.createRow(row + 3).apply {
                listOf(
                    File(item.path).nameWithoutExtension,
                    item.getDownloadSize(dexCompressedRatio).reportSize(),
                    item.getClassDownloadSize(dexCompressedRatio).reportSize(),
                    item.classSize.reportSize(),
                    item.nativeLibDownloadSize.reportSize(),
                    item.resourcesDownloadSize.reportSize(),
                    item.assetsDownloadSize.reportSize(),
                    item.othersDownloadSize.reportSize(),
                    item.path.substring(item.path.indexOf("files-2.1/") + 9)
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }
        }
    }

    private fun sortData(dexCompressedRatio: Double, contributor: Set<Contributor>): List<Contributor> {
        val data = contributor.toList()
        Collections.sort(data, Comparator<Contributor> { o1, o2 ->
            val size1 = o1.getDownloadSize(dexCompressedRatio)
            val size2 = o2.getDownloadSize(dexCompressedRatio)
            if (size1 > size2) -1
            else if (size1 < size2) 1
            else 0
        })
        return data
    }

    private fun Long.reportSize(): String = when {
        this < KILO_BYTE -> "$this bytes"
        this < MEGA_BYTE -> "%.3f KB".format(this.toDouble() / KILO_BYTE)
        else -> "%.3f MB".format(this.toDouble() / MEGA_BYTE)
    }

}
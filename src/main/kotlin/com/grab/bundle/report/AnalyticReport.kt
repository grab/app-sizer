package com.grab.bundle.report

import com.grab.bundle.Contributor
import com.grab.bundle.RawFileInfo
import com.grab.bundle.apk.ApkFileInfo
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.util.*

interface AnalyticReport {
    fun report(androidBinaryInfo: Set<ApkFileInfo>, contributor: Set<Contributor>)
}


private const val KILO_BYTE = 1024L
private const val MEGA_BYTE = 1024L * 1024L

class GroupByLibAnalyticReport(
    private val outPutFile: File
) : AnalyticReport {
    override fun report(apks: Set<ApkFileInfo>, contributor: Set<Contributor>) {
        val workbook = WorkbookFactory.create(false)
        val sheet = workbook.createSheet("Libraries contributors")
        createHeader(sheet)
        val dexCompressedRatio = dexCompressedRatio(apks)
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

    private fun dexCompressedRatio(apks: Set<ApkFileInfo>): Double {
        val dexCompressedSize = apks.flatMap { it.dexes }.sumOf { it.compressedSize }
        val dexSize = apks.flatMap { it.dexes }.sumOf { it.size }
        return dexCompressedSize.toDouble() / dexSize
    }

    private fun apksSizeReport(dexCompressedRatio: Double, apks: Set<ApkFileInfo>, sheet: Sheet) {
        val resourceSize = apks.flatMap { it.resources }.sumOf { it.compressedSize }
        val nativeLibSize = apks.flatMap { it.nativeLibs }.sumOf { it.compressedSize }
        val assetSizes = apks.flatMap { it.assets }.sumOf { it.compressedSize }
        val othersSize = apks.flatMap { it.others }.sumOf { it.compressedSize }
        val dexCompressedSize = apks.flatMap { it.dexes }.sumOf { it.compressedSize }
        val classesSize = apks.flatMap { it.dexes }.flatMap { it.classes }.sumOf { it.size }
        val total = resourceSize + nativeLibSize + assetSizes + othersSize + dexCompressedSize
        val classCompressSize = (classesSize * dexCompressedRatio).toLong()

        val others = apks.flatMap { it.others }.toList()
        Collections.sort(others, Comparator< RawFileInfo> { a, b ->
            if(a.compressedSize > b.compressedSize) -1
            else if(a.compressedSize < b.compressedSize) 1
            else 0
        })

        sheet.createRow(1).apply {
            listOf(
                "Apks",
                total.reportSize(),
                classCompressSize.reportSize(),
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
                    "All libs",
                    allLibs.getCompressedSize(dexCompressedRatio).reportSize(),
                    allLibs.getClassCompressedSize(dexCompressedRatio).reportSize(),
                    allLibs.classSize.reportSize(),
                    allLibs.nativeCompressedLibSize.reportSize(),
                    allLibs.resourceCompressedSize.reportSize(),
                    allLibs.assetsCompressedSize.reportSize(),
                    allLibs.othersCompressedSize.reportSize()
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
                    File(item.name).nameWithoutExtension,
                    item.getCompressedSize(dexCompressedRatio).reportSize(),
                    item.getClassCompressedSize(dexCompressedRatio).reportSize(),
                    item.classSize.reportSize(),
                    item.nativeCompressedLibSize.reportSize(),
                    item.resourceCompressedSize.reportSize(),
                    item.assetsCompressedSize.reportSize(),
                    item.othersCompressedSize.reportSize(),
                    item.name.replace(
                        "/Users/van.minh/Projects/apk-analytic/gradle-cache-5167/caches/modules-2/files-2.1/",
                        ""
                    )
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }
        }
    }

    private fun sortData(dexCompressedRatio: Double, contributor: Set<Contributor>): List<Contributor> {
        val data = contributor.toList()
        Collections.sort(data, Comparator<Contributor> { o1, o2 ->
            val size1 = o1.getCompressedSize(dexCompressedRatio)
            val size2 = o2.getCompressedSize(dexCompressedRatio)
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
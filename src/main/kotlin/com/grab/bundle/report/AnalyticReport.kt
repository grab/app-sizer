package com.grab.bundle.report

import com.grab.bundle.ApkFileInfo
import com.grab.bundle.Contributors
import com.grab.bundle.RawFileInfo
import com.grab.bundle.FileType
import com.grab.bundle.analytic.Analytic
import com.grab.bundle.log.log
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.util.*

interface AnalyticReport {
    fun report(androidBinaryInfo: ApkFileInfo, rawData: Map<String, Contributors>)
}

class BuildReportData(private val analytics: Map<String, Analytic>) {
    fun build(data: Map<String, Contributors>): List<LibraryInfo> {
        return mutableMapOf<String, LibraryInfo>().apply {
            analytics.forEach { (tag, _) ->
                // Get data from each analytic result
                data[tag]?.entries?.forEach { libEntry ->
                    putIfAbsent(libEntry.key, LibraryInfo(libEntry.key))
                    val libraryInfo = get(libEntry.key)
                    libEntry.value.forEach { fileItem ->
                        libraryInfo?.addFileInfo(fileItem)
                    }
                }
            }
        }.values.toList()
    }
}

private const val KILO_BYTE = 1024L
private const val MEGA_BYTE = 1024L * 1024L

class GroupByLibAnalyticReport(
    private val buildReportData: BuildReportData,
    private val outPutFile: File
) : AnalyticReport {
    override fun report(androidBinaryInfo: ApkFileInfo, rawData: Map<String, Contributors>) {

        val workbook = WorkbookFactory.create(false)

        val sheet = workbook.createSheet("Libraries contributors")
        createHeader(sheet)
        apksSizeReport(androidBinaryInfo, sheet)
        val data = sortData(rawData)
        totalLibsContributor(data, sheet)
        reportEachLib(data, sheet)

        outPutFile.outputStream().use {
            workbook.write(it)
        }

    }

    private fun createHeader(sheet: Sheet) {
        sheet.createRow(0).apply {
            listOf(
                "Library Name",
                "Total",
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

    private fun apksSizeReport(apkFilesInfo: ApkFileInfo, sheet: Sheet) {
        val resourceSize = apkFilesInfo[FileType.RESOURCE]?.sumOf { it.size } ?: 0
        val nativeLibSize = apkFilesInfo[FileType.NATIVE_LIB]?.sumOf { it.size } ?: 0
        val assetSizes = apkFilesInfo[FileType.ASSET]?.sumOf { it.size } ?: 0
        val othersSize = apkFilesInfo[FileType.OTHERS]?.sumOf { it.size } ?: 0
        val total = resourceSize + nativeLibSize + assetSizes + othersSize

        sheet.createRow(1).apply {
            listOf(
                "Combine All Apks",
                total.reportSize(),
                nativeLibSize.reportSize(),
                resourceSize.reportSize(),
                assetSizes.reportSize(),
                othersSize.reportSize()
            ).forEachIndexed { index, s ->
                createCell(index).apply { setCellValue(s) }
            }
        }
    }

    private fun totalLibsContributor(data: List<LibraryInfo>, sheet: Sheet) {
        data.reduce { pre, cur ->
            pre.copy(
                size = pre.size + cur.size,
                resourceSize = pre.resourceSize + cur.resourceSize,
                assetsSize = pre.assetsSize + cur.assetsSize,
                nativeLibSize = pre.nativeLibSize + cur.nativeLibSize,
                othersSize = pre.othersSize + cur.othersSize
            )
        }.also { app ->
            sheet.createRow(2).apply {
                listOf(
                    "All libs",
                    app.size.reportSize(),
                    app.nativeLibSize.reportSize(),
                    app.resourceSize.reportSize(),
                    app.assetsSize.reportSize(),
                    app.othersSize.reportSize()
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }

                log("Lib name : All libs")
                log("Lib size : ${app.size.reportSize()}")
                log("Lib resourceSize : ${app.resourceSize.reportSize()}")
                log("Lib assetsSize : ${app.assetsSize.reportSize()}")
                log("Lib nativeLibSize : ${app.nativeLibSize.reportSize()}")
                log("Lib othersSize : ${app.othersSize.reportSize()}")
            }
        }
    }

    private fun reportEachLib(data: List<LibraryInfo>, sheet: Sheet) {
        data.forEachIndexed { row, item ->
            sheet.createRow(row + 3).apply {
                listOf(
                    File(item.name).nameWithoutExtension,
                    item.size.reportSize(),
                    item.nativeLibSize.reportSize(),
                    item.resourceSize.reportSize(),
                    item.assetsSize.reportSize(),
                    item.othersSize.reportSize(),
                    item.name.replace(
                        "/Users/van.minh/Projects/pax-android-v2/gradle-cache/caches/modules-2/files-2.1/",
                        ""
                    )
                ).forEachIndexed { index, s ->
                    createCell(index).apply { setCellValue(s) }
                }
            }

            log("Lib name : ${item.name}")
            log("Lib size : ${item.size.reportSize()}")
            log("Lib resourceSize : ${item.resourceSize.reportSize()}")
            log("Lib assetsSize : ${item.assetsSize.reportSize()}")
            log("Lib nativeLibSize : ${item.nativeLibSize.reportSize()}")
            log("Lib othersSize : ${item.othersSize.reportSize()}")
        }
    }

    private fun sortData(rawData: Map<String, Contributors>): List<LibraryInfo> {
        val data = buildReportData.build(rawData)
        Collections.sort(data, Comparator<LibraryInfo> { o1, o2 ->
            if (o1.size > o2.size) -1
            else if (o1.size < o2.size) 1
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

data class LibraryInfo(
    val name: String,
    var size: Long = 0,
    var resourceSize: Long = 0,
    var nativeLibSize: Long = 0,
    var assetsSize: Long = 0,
    var othersSize: Long = 0,
    val resources: MutableSet<RawFileInfo> = mutableSetOf(),
    val nativeLib: MutableSet<RawFileInfo> = mutableSetOf(),
    val assets: MutableSet<RawFileInfo> = mutableSetOf(),
    val others: MutableSet<RawFileInfo> = mutableSetOf()
) {
    fun addFileInfo(rawFile: RawFileInfo) {
        size += rawFile.size
        when (rawFile.type) {
            FileType.RESOURCE -> {
                resources.add(rawFile)
                resourceSize += rawFile.size
            }
            FileType.NATIVE_LIB -> {
                nativeLib.add(rawFile)
                nativeLibSize += rawFile.size
            }
            FileType.ASSET -> {
                assets.add(rawFile)
                assetsSize += rawFile.size
            }
            else -> {
                others.add(rawFile)
                othersSize += rawFile.size
            }
        }
    }
}
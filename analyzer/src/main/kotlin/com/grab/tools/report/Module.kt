package com.grab.tools.report

import com.grab.tools.analyzer.report.ReportItem
import com.grab.tools.model.Contributor

data class Module(
    val name: String,
    val contributors: List<Contributor>
) {
    val resourcesDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.resourcesDownloadSize } }
    val nativeLibDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.nativeLibDownloadSize } }
    val assetsDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.assetsDownloadSize } }
    val othersDownloadSize: Long by lazy { contributors.sumOf { contributor -> contributor.othersDownloadSize } }
    val classSize: Long by lazy { contributors.sumOf { contributor -> contributor.classSize } }

    fun getClassDownloadSize(downloadSizeRatio: Double): Long = (classSize * downloadSizeRatio).toLong()

    fun getDownloadSize(downloadSizeRatio: Double): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + getClassDownloadSize(
            downloadSizeRatio
        )


    fun toReportItem(dexCompressedRatio: Double, moduleToFeatureMap: Map<String, String>): ReportItem =
        ReportItem(
            name = name,
            id = name,
            owner = moduleToFeatureMap[name],
            extraInfo = "Sum up all codebase for $name",
            totalDownloadSize = getDownloadSize(dexCompressedRatio),
            classesSize = classSize,
            classesDownloadSize = getClassDownloadSize(dexCompressedRatio),
            nativeLibDownloadSize = nativeLibDownloadSize,
            resourceDownloadSize = resourcesDownloadSize,
            assetDownloadSize = assetsDownloadSize,
            otherDownloadSize = othersDownloadSize
        )
}


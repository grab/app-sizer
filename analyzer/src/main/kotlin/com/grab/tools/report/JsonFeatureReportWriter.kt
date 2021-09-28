package com.grab.tools.report

import java.io.File

class JsonFeatureReportWriter(
    private val outputFile: File
) : FeatureReportWriter {
    override fun initTile() {
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

    }

    override fun reportTotalFeatures(dexCompressedRatio: Double, allFeatures: Feature) {
    }

    override fun reportEachFeature(dexCompressedRatio: Double, data: List<Feature>) {
    }

    override fun save() {
    }
}
package com.grab.tools.report

interface FeatureReportWriter {
    fun initTile()
    fun reportApksSize(
        total: Long,
        classDownloadSize: Long,
        classesSize: Long,
        nativeLibSize: Long,
        resourceSize: Long,
        assetSizes: Long,
        othersSize: Long
    )

    fun reportTotalFeatures(dexCompressedRatio: Double, allFeatures: Feature)
    fun reportEachFeature(dexCompressedRatio: Double, data: List<Feature>)
    fun save()
}


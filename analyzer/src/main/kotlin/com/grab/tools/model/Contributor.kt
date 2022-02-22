package com.grab.tools.model

data class Contributor(
    val path: String,
    val assets: Set<RawFileInfo> = emptySet(),
    val resources: Set<RawFileInfo> = emptySet(),
    val nativeLibs: Set<RawFileInfo> = emptySet(),
    val classes: Set<ClassFileInfo> = emptySet(),
    val others: Set<RawFileInfo> = emptySet(),
) {
    val resourcesDownloadSize: Long by lazy { resources.sumOf { resource -> resource.downloadSize } }
    val nativeLibDownloadSize: Long by lazy { nativeLibs.sumOf { lib -> lib.downloadSize } }
    val assetsDownloadSize: Long by lazy { assets.sumOf { asset -> asset.downloadSize } }
    val othersDownloadSize: Long by lazy { others.sumOf { other -> other.downloadSize } }
    val classSize: Long by lazy { classes.sumOf { clazz -> clazz.size } }

    fun getClassDownloadSize(downloadSizeRatio: Double): Long = (classSize * downloadSizeRatio).toLong()

    fun getDownloadSize(downloadSizeRatio: Double): Long =
        resourcesDownloadSize + nativeLibDownloadSize + assetsDownloadSize + othersDownloadSize + getClassDownloadSize(
            downloadSizeRatio
        )

    override fun equals(other: Any?): Boolean {
        if (other is Contributor) {
            return path == other.path
        }

        return super.equals(other)
    }

    override fun hashCode(): Int = path.hashCode()
}
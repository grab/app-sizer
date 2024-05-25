package com.grab.sizer.analyzer.model

/**
 * Represents a aar or a jar file, and their components (assets, resources, native libraries, classes, and others).
 * These component are files and classes, each component should provide the sizes it contributes to the apk.
 *
 * @property path the path to the aar/jar file
 * @property assets a set of assets files
 * @property resources a set of resources files
 * @property nativeLibs a set of native libraries files (*.so files)
 * @property classes a set of classes
 * @property others a set of other files not categorized as assets, resources, native libraries or classes.
 */
data class Contributor(
    val path: String,
    val assets: Set<RawFileInfo> = emptySet(),
    val resources: Set<RawFileInfo> = emptySet(),
    val nativeLibs: Set<RawFileInfo> = emptySet(),
    val classes: Set<ClassFileInfo> = emptySet(),
    val others: Set<RawFileInfo> = emptySet(),
) {
    // Calculates the sum of the download sizes of all resources
    val resourcesDownloadSize: Long by lazy { resources.sumOf { resource -> resource.downloadSize } }

    // Calculates the sum of the download sizes of all native libraries
    val nativeLibDownloadSize: Long by lazy { nativeLibs.sumOf { lib -> lib.downloadSize } }

    // Calculates the sum of the download sizes of all assets
    val assetsDownloadSize: Long by lazy { assets.sumOf { asset -> asset.downloadSize } }

    // Calculates the sum of the download sizes of all "other" files
    val othersDownloadSize: Long by lazy { others.sumOf { other -> other.downloadSize } }

    // Calculates the sum of the sizes of all classes
    val classSize: Long by lazy { classes.sumOf { clazz -> clazz.size } }

    /**
     * Calculates the download size of all classes by using a given ratio.
     *
     * @param downloadSizeRatio the ratio to multiply with the size of each class.
     * @return the total downloadable size of all classes.
     */
    fun getClassDownloadSize(downloadSizeRatio: Double): Long = (classSize * downloadSizeRatio).toLong()

    /**
     * Calculates the total downloadable size of all component types (assets, resources, native libraries, classes, others).
     *
     * @param downloadSizeRatio the ratio to calculate downloadable size for classes.
     * @return the total downloadable size of all components.
     */
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
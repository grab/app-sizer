package com.grab.bundle

data class Contributor(
    val name: String,
    val assets: Set<RawFileInfo> = emptySet(),
    val resources: Set<RawFileInfo> = emptySet(),
    val nativeLibs: Set<RawFileInfo> = emptySet(),
    val classes: Set<ClassFileInfo> = emptySet(),
    val others: Set<RawFileInfo> = emptySet(),
) {
    val resourceCompressedSize: Long by lazy { resources.sumOf { resource -> resource.compressedSize } }
    val nativeCompressedLibSize: Long by lazy { nativeLibs.sumOf { lib -> lib.compressedSize } }
    val assetsCompressedSize: Long by lazy { assets.sumOf { asset -> asset.compressedSize } }
    val othersCompressedSize: Long by lazy { others.sumOf { other -> other.compressedSize } }
    val classSize: Long by lazy { classes.sumOf { clazz -> clazz.size } }

    fun getClassCompressedSize(ratio : Double): Long = (classSize * ratio).toLong()

    fun getCompressedSize(ratio : Double) : Long =
        resourceCompressedSize + nativeCompressedLibSize + assetsCompressedSize + othersCompressedSize + getClassCompressedSize(ratio)

    override fun equals(other: Any?): Boolean {
        if (other is Contributor) {
            return name == other.name
        }

        return super.equals(other)
    }

    override fun hashCode(): Int = name.hashCode()
}
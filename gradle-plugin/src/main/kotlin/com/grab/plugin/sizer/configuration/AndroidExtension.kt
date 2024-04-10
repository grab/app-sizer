package com.grab.plugin.sizer.configuration

import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.BuildType
import com.android.builder.model.ProductFlavor
import org.gradle.api.Action
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class AndroidExtension @Inject constructor(objects: ObjectFactory) {
    var apk: ApkGeneratorConfig = objects.newInstance(ApkGeneratorConfig::class.java, objects)
    var variantFilter: Action<VariantFilter>? = null

    fun variantFilter(action: Action<VariantFilter>) {
        variantFilter = action
    }

    fun apk(action: Action<in ApkGeneratorConfig>) {
        action.execute(apk)
    }

    fun apk(block: ApkGeneratorConfig.() -> Unit) {
        block(apk)
    }
}

interface VariantFilter {
    fun setIgnore(ignore: Boolean)
    val buildType: BuildType
    val flavors: List<ProductFlavor>
    val name: String
}

internal class DefaultVariantFilter(variant: BaseVariant) : VariantFilter {
    var ignored: Boolean = false
    override fun setIgnore(ignore: Boolean) {
        ignored = ignore
    }

    override val buildType: BuildType = variant.buildType
    override val flavors: List<ProductFlavor> = variant.productFlavors
    override val name: String = variant.name
}
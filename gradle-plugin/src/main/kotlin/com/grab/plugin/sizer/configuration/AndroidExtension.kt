package com.grab.plugin.sizer.configuration

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class AndroidExtension @Inject constructor(objects: ObjectFactory){
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

interface VariantFilter{

}
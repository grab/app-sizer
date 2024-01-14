package com.grab.sizer.report

import org.yaml.snakeyaml.Yaml
import java.io.File


interface FeatureMapping {
    val featureToModuleMap: Map<String, List<String>>
    val moduleToFeatureMap: Map<String, String>
}

class DummyFeatureMapping : FeatureMapping {
    override val featureToModuleMap: Map<String, List<String>> = emptyMap()
    override val moduleToFeatureMap: Map<String, String> = emptyMap()
}

class YmlFeatureMapping(
    private val ymlFile: File
) : FeatureMapping {
    override val featureToModuleMap: Map<String, List<String>> by lazy {
        loadFeatureToModuleMap()
    }
    override val moduleToFeatureMap: Map<String, String> by lazy {
        mutableMapOf<String, String>().apply {
            featureToModuleMap.forEach { (feature, modules) ->
                modules.forEach { put(it, feature) }
            }
        }
    }

    private fun loadFeatureToModuleMap(): Map<String, List<String>> = Yaml().load(ymlFile.inputStream())
}
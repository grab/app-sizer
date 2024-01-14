package com.grab.plugin.sizer.dependencies

import com.grab.plugin.sizer.AppSizeTaskScope
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import javax.inject.Inject

interface ConfigurationExtractor {
    fun runtimeConfigurations(project: Project): Sequence<Configuration>
}

@AppSizeTaskScope
internal class DefaultConfigurationExtractor @Inject constructor(
    private val variantExtractor: VariantExtractor
) : ConfigurationExtractor {
    override fun runtimeConfigurations(project: Project): Sequence<Configuration> {
        val variant = variantExtractor.findMatchVariant(project)
        return project.configurations.asSequence()
            .filter {
                variant.runtimeConfiguration.hierarchy.contains(it)
            }
    }
}


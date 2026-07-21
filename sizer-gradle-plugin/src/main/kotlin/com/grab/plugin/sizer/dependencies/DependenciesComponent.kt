/*
 * MIT License
 *
 * Copyright (c) 2024.  Grabtaxi Holdings Pte Ltd (GRAB), All rights reserved.
 *
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.grab.plugin.sizer.dependencies

import com.grab.plugin.sizer.utils.DefaultPluginLogger
import com.grab.plugin.sizer.utils.PluginLogger
import org.gradle.api.Project

/**
 * Hand-wired object graph for the dependency extraction of one variant, replacing the
 * previous Dagger component. Dagger was removed from the plugin because the buildscript
 * classpath offers no dependency isolation, so another plugin's Dagger version can clash
 * with the generated code at runtime.
 */
internal class DependenciesComponent(
    project: Project,
    variantInput: VariantInput,
    flavorMatchingFallbacks: List<String>,
    buildTypeMatchingFallbacks: List<String>,
    enableMatchDebugVariant: Boolean,
) {
    private val logger: PluginLogger = DefaultPluginLogger(project)

    val variantExtractor: VariantExtractor = DefaultVariantExtractor(
        variantInput = variantInput,
        flavorMatchingFallbacks = flavorMatchingFallbacks,
        buildTypeMatchingFallbacks = buildTypeMatchingFallbacks,
        enableMatchDebugVariant = enableMatchDebugVariant,
    )

    val configurationExtractor: ConfigurationExtractor =
        DefaultConfigurationExtractor(variantExtractor, logger)

    val dependencyExtractor: DependencyExtractor = DefaultDependencyExtractor(
        appProject = project,
        configurationExtractor = configurationExtractor,
        archiveExtractor = DefaultArchiveExtractor(variantExtractor),
        logger = logger,
    )
}

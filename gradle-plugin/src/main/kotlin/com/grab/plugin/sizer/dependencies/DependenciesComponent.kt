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

import com.grab.plugin.sizer.utils.PluginLogger
import com.grab.sizer.utils.Logger
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.gradle.api.Project
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
internal annotation class DependenciesScope

@Component(
    modules = [DependenciesModule::class]
)
@DependenciesScope
internal interface DependenciesComponent {
    fun dependencyExtractor(): DependencyExtractor
    fun configurationExtractor(): ConfigurationExtractor
    fun variantExtractor(): VariantExtractor

    fun logger(): Logger

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance project: Project,
            @BindsInstance variantInput: VariantInput,
            @BindsInstance @Named(BUILD_FLAVOR) flavorMatchingFallbacks: List<String>,
            @BindsInstance @Named(BUILD_TYPE) buildTypeMatchingFallbacks: List<String>,
            @BindsInstance @Named(ENABLE_MATCH_DEBUG_VARIANT) enableMatchDebugVariant: Boolean
        ): DependenciesComponent
    }
}

@Module
internal interface DependenciesModule {
    @Binds
    fun bindArchiveExtractor(extractor: DefaultArchiveExtractor): ArchiveExtractor

    @Binds
    fun bindConfigurationExtractor(extractor: DefaultConfigurationExtractor): ConfigurationExtractor

    @Binds
    fun bindDependencyExtractor(extractor: DefaultDependencyExtractor): DependencyExtractor

    @Binds
    fun bindVariantExtractor(extractor: DefaultVariantExtractor): VariantExtractor

    @Binds
    fun bindLogger(logger: PluginLogger): Logger
}

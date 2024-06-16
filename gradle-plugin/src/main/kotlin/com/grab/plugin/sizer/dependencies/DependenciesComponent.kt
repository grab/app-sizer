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

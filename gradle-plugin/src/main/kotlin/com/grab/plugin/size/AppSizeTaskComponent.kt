package com.grab.plugin.size

import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.dependencies.*
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.gradle.api.Project
import javax.inject.Named
import javax.inject.Scope

@Scope
@Retention(AnnotationRetention.RUNTIME)
internal annotation class AppSizeTaskScope

@Component(
    modules = [AppSizeTaskModule::class]
)
@AppSizeTaskScope
internal interface AppSizeTaskComponent {
    fun dependencyExtractor() : DependencyExtractor
    fun buildVariant() : BaseVariant
    fun configurationExtractor() : ConfigurationExtractor
    fun variantExtractor() : VariantExtractor

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance project: Project,
            @BindsInstance variant: BaseVariant,
            @BindsInstance @Named(BUILD_FLAVOR) flavorMatchingFallbacks: List<String>,
            @BindsInstance @Named(BUILD_TYPE) buildTypeMatchingFallbacks: List<String>
        ) : AppSizeTaskComponent
    }
}

@Module
internal interface AppSizeTaskModule {
    @Binds
    fun DefaultArchiveExtractor.bindArchiveExtractor(): ArchiveExtractor

    @Binds
    fun DefaultConfigurationExtractor.bindConfigurationExtractor(): ConfigurationExtractor

    @Binds
    fun DefaultDependencyExtractor.bindDependencyExtractor(): DependencyExtractor

    @Binds
    fun DefaultVariantExtractor.bindVariantExtractor(): VariantExtractor
}

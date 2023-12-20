package com.grab.plugin.size.dependencies

import com.grab.plugin.size.AppSizeTaskScope
import com.grab.plugin.size.utils.isAndroidApplication
import com.grab.plugin.size.utils.isAndroidLibrary
import com.grab.plugin.size.utils.isKotlinJvm
import org.gradle.api.Project
import javax.inject.Inject

interface ArchiveExtractor {
    fun extract(project: Project): ArchiveDependency
}

@AppSizeTaskScope
internal class DefaultArchiveExtractor @Inject constructor(
    private val variantExtractor: VariantExtractor
) : ArchiveExtractor {
    override fun extract(project: Project): ArchiveDependency {
        val matchVariant = variantExtractor.findMatchVariant(project)
        when {
            project.isAndroidApplication -> {
                return AppDependency(
                    name = project.path,
                    pathToArtifact = matchVariant.binaryOutPut.path
                )
            }

            project.isAndroidLibrary -> {
                return ModuleDependency(
                    name = project.path,
                    pathToArtifact = matchVariant.binaryOutPut.path
                )
            }

            project.isKotlinJvm -> {
                return JavaModuleDependency(
                    name = project.path,
                    pathToArtifact = matchVariant.binaryOutPut.path
                )
            }

            else -> {
                throw IllegalArgumentException("The ${project.name} is not an Android/Kotlin module")
            }
        }
    }
}
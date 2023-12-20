package com.grab.plugin.size.dependencies

import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.size.AppSizeTaskScope
import com.grab.plugin.size.utils.isAndroidApplication
import com.grab.plugin.size.utils.isAndroidLibrary
import com.grab.plugin.size.utils.isJava
import com.grab.plugin.size.utils.isKotlinJvm
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.the
import java.io.File
import javax.inject.Inject
import javax.inject.Named


internal const val BUILD_TYPE = "BUILD_TYPE"
internal const val BUILD_FLAVOR = "BUILD_FLAVOR"

internal interface VariantExtractor {
    fun findMatchVariant(project: Project): AppSizeVariant
}

internal interface AppSizeVariant {
    val binaryOutPut: File
    val runtimeConfiguration: Configuration
}

@AppSizeTaskScope
internal class DefaultVariantExtractor @Inject constructor(
    private val baseVariant: BaseVariant,
    @Named(BUILD_FLAVOR)
    private val flavorMatchingFallbacks: List<String>,
    @Named(BUILD_TYPE)
    private val buildTypeMatchingFallbacks: List<String>
) : VariantExtractor {
    override fun findMatchVariant(project: Project): AppSizeVariant {
        return when {
            project.isAndroidApplication -> AndroidAppSizeVariant(baseVariant)
            project.isAndroidLibrary -> AndroidAppSizeVariant(extractLibVariant(project))
            project.isJava || project.isKotlinJvm -> JarAppSizeVariant(project)
            else -> {
                throw IllegalArgumentException("${project.name} is not supported")
            }
        }
    }

    private fun extractLibVariant(project: Project): BaseVariant {
        val extension = project.the<LibraryExtension>()
        val fullMatch = extension.libraryVariants.find { variant ->
            variant.name == baseVariant.name
        }
        if (fullMatch != null) return fullMatch

        val matchFlavorVariant = extension.libraryVariants.filter { variant ->
            variant.flavorName == baseVariant.flavorName
        }
        // Found matched flavor
        if (matchFlavorVariant.isNotEmpty()) {
            matchFlavorVariant.forEach {
                // match both, buildType & flavor
                if (it.buildType.name == baseVariant.buildType.name)
                    return it
            }

            // Find first match build type
            buildTypeMatchingFallbacks.forEach { fallBack ->
                matchFlavorVariant.forEach { variant ->
                    if (variant.buildType.name == fallBack) return variant
                }
            }
        }

        // No flavor matched have found
        val matchBuildType = extension.libraryVariants.filter { variant ->
            variant.buildType.name == baseVariant.buildType.name
        }
        if (matchBuildType.isNotEmpty()) {
            if (matchBuildType.size == 1) return matchBuildType.first()
            flavorMatchingFallbacks.forEach { fallback ->
                matchBuildType.forEach { variant ->
                    if (fallback == variant.flavorName) return variant
                }
            }
        }
        throw RuntimeException("Can not find the matching variant for ${project.name}")
    }
}

internal class JarAppSizeVariant(
    private val project: Project
) : AppSizeVariant {
    override val binaryOutPut: File
        get() {
            val jarTask = project.tasks.findByName(JavaPlugin.JAR_TASK_NAME) as Jar
            return jarTask.archiveFile.get().asFile
        }

    override val runtimeConfiguration: Configuration by lazy {
        project.configurations.first {
            it.name.contains("RuntimeClasspath", true)
        }
    }
}

internal class AndroidAppSizeVariant(
    val baseVariant: BaseVariant
) : AppSizeVariant {
    override val binaryOutPut: File
        get() = baseVariant.outputs.first().outputFile
    override val runtimeConfiguration: Configuration
        get() = baseVariant.runtimeConfiguration
}
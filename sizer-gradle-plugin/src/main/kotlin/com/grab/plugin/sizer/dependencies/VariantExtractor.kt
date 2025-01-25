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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.api.BaseVariant
import com.grab.plugin.sizer.utils.isAndroidApplication
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import com.grab.plugin.sizer.utils.isKotlinMultiplatform
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.the
import java.io.File
import java.io.Serializable
import javax.inject.Inject
import javax.inject.Named


internal const val BUILD_TYPE = "BUILD_TYPE"
internal const val BUILD_FLAVOR = "BUILD_FLAVOR"
internal const val ENABLE_MATCH_DEBUG_VARIANT = "ENABLE_MATCH_DEBUG_VARIANT"
internal const val BUILD_TYPE_DEBUG = "debug"

internal interface VariantExtractor {
    /**
     * This method finds a matching variant for a provided project.
     * It supports Android Applications, Android Libraries, Java and Kotlin JVM projects.
     *
     * @param project The project for which to locate the matching variant.
     * @return AppSizeVariant that is the extracted variant for the given project type.
     * @throws IllegalArgumentException if the project type is not supported.
     */
    fun findMatchVariant(project: Project): AppSizeVariant
}

internal interface AppSizeVariant {
    val binaryOutPut: File
    val runtimeConfiguration: Configuration
    val buildType: String
    val buildFlavor: String
}

data class VariantInput(
    val name: String,
    val flavorName: String,
    val buildTypeName: String,
    val versionName: String?
) : Serializable

internal fun BaseVariant.toVariantInput() = VariantInput(
    name = name,
    flavorName = flavorName,
    buildTypeName = buildType.name,
    versionName = mergedFlavor.versionName,
)


/**
 * DefaultVariantExtractor class is designed to extract matching variant and debug variant from various project types.
 * This class supports Android Applications, Android Libraries, Java and Kotlin JVM projects.
 * Based on the enableMatchDebugVariant flag, this implement of VariantExtractor will return the variant accordingly
 * - enableMatchDebugVariant is true, the variant debug build type will be selected by default, and the flavor will be matched
 * - enableMatchDebugVariant is false, the build type and flavor will be taken into account
 * @property variantInput references the base variant info being used for matching.
 * @property flavorMatchingFallbacks references list of build flavors to be used as fallbacks.
 * @property buildTypeMatchingFallbacks references list of build types to be used as fallbacks.
 * @property enableMatchDebugVariant specifies whether to match debug variant.
 */
@DependenciesScope
internal class DefaultVariantExtractor @Inject constructor(
    private val variantInput: VariantInput,
    @Named(BUILD_FLAVOR)
    private val flavorMatchingFallbacks: List<String>,
    @Named(BUILD_TYPE)
    private val buildTypeMatchingFallbacks: List<String>,
    @Named(ENABLE_MATCH_DEBUG_VARIANT)
    private val enableMatchDebugVariant: Boolean,
) : VariantExtractor {

    override fun findMatchVariant(project: Project): AppSizeVariant{
        return when{
            enableMatchDebugVariant -> findMatchDebugVariant(project)
            else -> defaultFindMatchVariant(project)
        }
    }

    /**
     * This method finds a matching variant for a provided project.
     * It supports Android Applications, Android Libraries, Java and Kotlin JVM projects.
     *
     * @param project The project for which to locate the matching variant.
     * @return AppSizeVariant that is the extracted variant for the given project type.
     * @throws IllegalArgumentException if the project type is not supported.
     */
    private fun defaultFindMatchVariant(project: Project): AppSizeVariant {
        return when {
            project.isAndroidApplication -> AndroidAppSizeVariant(
                project.extractVariant(project.the<AppExtension>().applicationVariants)
            )

            project.isAndroidLibrary -> AndroidAppSizeVariant(
                project.extractVariant(project.the<LibraryExtension>().libraryVariants)
            )

            project.isJava || project.isKotlinJvm -> JarAppSizeVariant(project)

            project.isKotlinMultiplatform -> JarAppSizeVariant(project, "jvmJar", "jvmRuntimeClasspath")

            else -> {
                throw IllegalArgumentException("${project.name} is not supported")
            }
        }
    }

    /**
     * This method finds the debug variant for a provided project.
     * It supports Android Applications, Android Libraries, Java and Kotlin JVM projects.
     *
     * @param project The project for which to locate the debug variant.
     * @return AppSizeVariant that is the debug variant for the given project type.
     * @throws IllegalArgumentException if the project type is not supported.
     */
    private fun findMatchDebugVariant(project: Project): AppSizeVariant {
        return when {
            project.isAndroidApplication -> AndroidAppSizeVariant(
                findDebugVariant(project.the<AppExtension>().applicationVariants)
            )

            project.isAndroidLibrary -> AndroidAppSizeVariant(
                findDebugVariant(project.the<LibraryExtension>().libraryVariants)
            )

            project.isJava || project.isKotlinJvm -> JarAppSizeVariant(project)

            project.isKotlinMultiplatform -> JarAppSizeVariant(project, "jvmJar", "jvmRuntimeClasspath")

            else -> {
                throw IllegalArgumentException("${project.name} is not supported")
            }
        }
    }

    /**
     * This function finds the debug variant that matches the flavor of the base variant.
     *
     * @param variants DomainObjectSet of BaseVariants that should be searched.
     * @return BaseVariant that is the debug variant matching the flavor of the base variant.
     * @throws RuntimeException if no matching debug variant can be found.
     */
    private fun findDebugVariant(variants: DomainObjectSet<out BaseVariant>): BaseVariant {
        // Filter out the debug variants from the provided set of variants.
        val debugVariants = variants.filter { variant ->
            variant.buildType.name == BUILD_TYPE_DEBUG
        }
        // Try finding a debug variant that matches the flavor of the base variant.
        val matchFlavor = debugVariants.find { variant ->
            variant.flavorName == variantInput.flavorName
        }

        // If a match is found, return it.
        if (matchFlavor != null) return matchFlavor

        // If there is only one debug variant, return it.
        if (debugVariants.isNotEmpty()) {
            if (debugVariants.size == 1) return debugVariants.first()

            // If there are multiple debug variants, use the presets in flavorMatchingFallbacks
            // to determine the best match.
            flavorMatchingFallbacks.forEach { fallback ->
                debugVariants.forEach { variant ->
                    if (fallback == variant.flavorName) return variant
                }
            }
        }
        // If no match was found, throw an exception.
        throw RuntimeException("Can not find the matching debug variant")
    }


    /**
     * This function extracts a variant that matches the base variant's flavor and build type.
     *
     * @receiver Project The project from which to extract the variant.
     * @return BaseVariant that is the variant matching the flavor and build type of the base variant.
     * @throws RuntimeException if no matching variant can be found.
     */
    private fun Project.extractVariant(variants: DomainObjectSet<out BaseVariant>): BaseVariant {

        // Try to find a variant that fully matches the base variant
        val fullMatch = variants.find { variant ->
            variant.name == variantInput.name
        }

        // If a full match is found, return it
        if (fullMatch != null) return fullMatch

        // Filter variants that has the same flavor as base variant
        val matchFlavorVariant = variants.filter { variant ->
            variant.flavorName == variantInput.flavorName
        }

        // If we found matching flavor
        if (matchFlavorVariant.isNotEmpty()) {
            // Find the build type that matches the base variant
            matchFlavorVariant.forEach {
                // match both, buildType & flavor
                if (it.buildType.name == variantInput.buildTypeName)
                    return it
            }

            // If no full match is found, match just by build type with our fallbacks
            buildTypeMatchingFallbacks.forEach { fallback ->
                matchFlavorVariant.forEach { variant ->
                    if (variant.buildType.name == fallback) return variant
                }
            }
        }

        // If no variant with matching flavor is found, filter by build type
        val matchBuildType = variants.filter { variant ->
            variant.buildType.name == variantInput.buildTypeName
        }

        // If found, return; if there are multiple matches, find the first match flavor by our fallbacks
        if (matchBuildType.isNotEmpty()) {
            if (matchBuildType.size == 1) return matchBuildType.first()
            flavorMatchingFallbacks.forEach { fallback ->
                matchBuildType.forEach { variant ->
                    if (fallback == variant.flavorName) return variant
                }
            }
        }

        // When no flavor or build type match, return debug by default
        val matchDefaultBuildType = variants.filter { variant ->
            variant.buildType.name == BUILD_TYPE_DEBUG
        }

        // If found, return; if there are multiple matches, find the first match flavor by our fallbacks
        if (matchDefaultBuildType.isNotEmpty()) {
            if (matchDefaultBuildType.size == 1) return matchDefaultBuildType.first()
            flavorMatchingFallbacks.forEach { fallback ->
                matchDefaultBuildType.forEach { variant ->
                    if (fallback == variant.flavorName) return variant
                }
            }
        }
        // When no match found, throw exception
        throw RuntimeException("Can not find the matching variant for ${project.name}")
    }
}

internal class JarAppSizeVariant(
    private val project: Project,
    private val taskName: String = JavaPlugin.JAR_TASK_NAME,
    private val configurationName: String = "RuntimeClasspath"
) : AppSizeVariant {
    override val binaryOutPut: File
        get() {
            val jarTask = project.tasks.findByName(taskName) as Jar
            return jarTask.archiveFile.get().asFile
        }

    override val runtimeConfiguration: Configuration by lazy {
        project.configurations.first {
            it.name.equals(configurationName, true)
        }
    }

    override val buildType: String
        get() = ""
    override val buildFlavor: String
        get() = ""
}

internal class AndroidAppSizeVariant(
    val baseVariant: BaseVariant
) : AppSizeVariant {
    override val binaryOutPut: File
        get() = baseVariant.outputs.first().outputFile
    override val runtimeConfiguration: Configuration
        get() = baseVariant.runtimeConfiguration
    override val buildType: String
        get() = baseVariant.buildType.name
    override val buildFlavor: String
        get() = baseVariant.flavorName
}
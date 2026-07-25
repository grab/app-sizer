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

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationVariant
import com.grab.plugin.sizer.utils.PluginLogger
import com.grab.plugin.sizer.utils.capitalize
import com.grab.plugin.sizer.utils.isAndroidLibrary
import com.grab.plugin.sizer.utils.isJava
import com.grab.plugin.sizer.utils.isKotlinJvm
import com.grab.plugin.sizer.utils.isKotlinMultiplatform
import com.grab.plugin.sizer.utils.warn
import org.gradle.api.Project
import org.gradle.api.artifacts.ArtifactView
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.type.ArtifactTypeDefinition
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import java.io.File

private const val JVM_RUNTIME_CLASSPATH = "jvmRuntimeClasspath"
private const val JAVA_RUNTIME_CLASSPATH = "runtimeClasspath"
internal const val ARTIFACT_TYPE_AAR = "aar"
internal const val ARTIFACT_TYPE_JAR = "jar"

/**
 * Resolves the archives (AARs and JARs) contributing to an application variant through
 * lenient [ArtifactView]s of the variant's runtime classpath.
 *
 * Gradle's dependency resolution replaces the previous execution-time traversal of Project
 * instances: component identifiers distinguish project modules from external libraries,
 * transitive dependencies are covered by the resolution itself, and the views' file
 * collections carry the task dependencies required to build module archives. Nothing here
 * touches a Project at execution time, which makes the plugin compatible with the
 * configuration cache.
 *
 * @property archiveDependencies everything contributing to the variant, evaluated lazily
 *   at execution time from the resolution result
 * @property archiveFiles the archive files backing [archiveDependencies]; wiring this into
 *   a task's inputs makes Gradle build the module AARs/JARs before that task runs
 */
internal class VariantArtifacts(
    val archiveDependencies: Provider<List<ArchiveDependency>>,
    val archiveFiles: FileCollection,
) {
    companion object {

        /**
         * @param enableMatchDebugVariant when true, module archives are taken from the
         * debug build type of the same flavor (cheaper to build), while external libraries
         * are still resolved from the analyzed variant, matching the behavior of previous
         * plugin versions
         */
        fun create(
            project: Project,
            variant: ApplicationVariant,
            android: ApplicationExtension,
            enableMatchDebugVariant: Boolean,
            logger: PluginLogger,
        ): VariantArtifacts {
            val variantClasspath = project.configurations.getByName(variant.name + RUNTIME_CLASSPATH_SUFFIX)
            val moduleClasspath = when {
                enableMatchDebugVariant -> debugClasspath(project, variant, logger) ?: variantClasspath
                else -> variantClasspath
            }

            val moduleAars = artifactView(moduleClasspath, ARTIFACT_TYPE_AAR)
            val moduleJars = artifactView(moduleClasspath, ARTIFACT_TYPE_JAR)
            val libraryAars = artifactView(variantClasspath, ARTIFACT_TYPE_AAR)
            val libraryJars = artifactView(variantClasspath, ARTIFACT_TYPE_JAR)

            /*
             * AGP shares granular artifacts (classes, resources) between projects instead of
             * the assembled AAR, so no artifact view can return an Android module's AAR.
             * The AARs are wired from each module's bundle<Variant>Aar task once every
             * project is evaluated; configuration-time cross-project access is compatible
             * with the configuration cache (only execution-time access is not).
             */
            val aarByModule = project.objects.mapProperty(String::class.java, String::class.java)
            val moduleAarFiles = project.objects.fileCollection()
            val matcher = AndroidVariantMatcher(
                variantInput = VariantInput(variant.name, variant.flavorName.orEmpty(), variant.buildType.orEmpty()),
                flavorMatchingFallbacks = variant.flavorMatchingFallbacks(android),
                buildTypeMatchingFallbacks = variant.buildTypeMatchingFallbacks(android),
                enableMatchDebugVariant = enableMatchDebugVariant,
            )
            project.gradle.projectsEvaluated {
                wireAndroidModuleAars(project, moduleClasspath.name, matcher, aarByModule, moduleAarFiles, logger)
            }

            val appDependency = AppDependency(
                name = project.path.trim(':').ifEmpty { project.name },
                pathToArtifact = apkOutputDirectory(project, variant, enableMatchDebugVariant),
            )

            val moduleEntries = moduleAars.entries(ARTIFACT_TYPE_AAR)
                .zip(moduleJars.entries(ARTIFACT_TYPE_JAR)) { aars, jars -> aars + jars }
                .zip(aarByModule) { entries, aarPaths ->
                    val fromViews = entries
                        .filter { it !is ExternalDependency }
                        // Android modules surface through the views with granular artifacts
                        // (e.g. full.jar); their entry is replaced by the assembled AAR
                        .map { entry -> aarPaths[entry.name]?.let { ModuleDependency(entry.name, it) } ?: entry }
                        .distinctBy { it.name }
                    val covered = fromViews.map { it.name }.toSet()
                    fromViews + aarPaths.filterKeys { it !in covered }
                        .map { (name, path) -> ModuleDependency(name, path) }
                }
            val libraryEntries = libraryAars.entries(ARTIFACT_TYPE_AAR).zip(libraryJars.entries(ARTIFACT_TYPE_JAR)) { aars, jars ->
                (aars + jars).filterIsInstance<ExternalDependency>().distinctBy { it.name }
            }
            val archiveDependencies = moduleEntries.zip(libraryEntries) { modules, libraries ->
                listOf(appDependency) + modules + libraries
            }

            val archiveFiles = moduleAars.files
                .plus(moduleJars.files)
                .plus(libraryAars.files)
                .plus(libraryJars.files)
                .plus(moduleAarFiles)

            return VariantArtifacts(archiveDependencies, archiveFiles)
        }

        /**
         * Walks the declared project dependencies of the analyzed variant and wires the
         * matched bundle<Variant>Aar task of every participating Android library module:
         * its output path into [aarByModule] and its task dependency into [moduleAarFiles].
         */
        private fun wireAndroidModuleAars(
            app: Project,
            startConfiguration: String,
            matcher: AndroidVariantMatcher,
            aarByModule: MapProperty<String, String>,
            moduleAarFiles: ConfigurableFileCollection,
            logger: PluginLogger,
        ) {
            val visited = mutableSetOf(app.path)
            val queue = ArrayDeque(listOf(app to startConfiguration))
            while (queue.isNotEmpty()) {
                val (current, configurationName) = queue.removeFirst()
                val configuration = current.configurations.findByName(configurationName) ?: continue
                configuration.allDependencies.withType(ProjectDependency::class.java).forEach { dependency ->
                    val target = current.project(dependency.path)
                    if (!visited.add(target.path)) return@forEach
                    val nextConfiguration = when {
                        target.isAndroidLibrary -> {
                            val candidate = matcher.match(target)
                            when {
                                candidate == null -> {
                                    logger.warn("Could not match a variant for Android library ${target.path}; it is analyzed from its granular artifacts instead of its AAR")
                                    null
                                }

                                candidate.bundleAarTaskName !in target.tasks.names -> {
                                    logger.warn("Task ${candidate.bundleAarTaskName} does not exist in ${target.path}; the module is analyzed from its granular artifacts instead of its AAR")
                                    candidate.runtimeClasspathName
                                }

                                else -> {
                                    val bundleAar = target.tasks
                                        .named(candidate.bundleAarTaskName, AbstractArchiveTask::class.java)
                                    aarByModule.put(
                                        target.path.trim(':'),
                                        bundleAar.flatMap { it.archiveFile }.map { it.asFile.path }
                                    )
                                    moduleAarFiles.from(bundleAar)
                                    candidate.runtimeClasspathName
                                }
                            }
                        }

                        target.isKotlinMultiplatform -> JVM_RUNTIME_CLASSPATH
                        target.isJava || target.isKotlinJvm -> JAVA_RUNTIME_CLASSPATH
                        else -> null
                    }
                    if (nextConfiguration != null) queue.add(target to nextConfiguration)
                }
            }
        }

        /**
         * The runtime classpath of the debug variant with the same flavor, used to fetch
         * module archives when [create]'s enableMatchDebugVariant is set.
         */
        private fun debugClasspath(project: Project, variant: ApplicationVariant, logger: PluginLogger): Configuration? {
            val name = debugVariantName(variant) + RUNTIME_CLASSPATH_SUFFIX
            val configuration = project.configurations.findByName(name)
            if (configuration == null) {
                logger.warn(
                    "enableMatchDebugVariant is set but the $name configuration does not exist; " +
                            "module archives fall back to the ${variant.name} variant"
                )
            }
            return configuration
        }

        private fun debugVariantName(variant: ApplicationVariant): String = when {
            variant.buildType == BUILD_TYPE_DEBUG -> variant.name
            variant.flavorName.isNullOrEmpty() -> BUILD_TYPE_DEBUG
            else -> "${variant.flavorName}${BUILD_TYPE_DEBUG.capitalize()}"
        }

        /**
         * The conventional APK output directory recorded for the application module itself.
         * With enableMatchDebugVariant the debug directory is recorded, mirroring where the
         * previous implementation read the app output from.
         */
        private fun apkOutputDirectory(
            project: Project,
            variant: ApplicationVariant,
            enableMatchDebugVariant: Boolean,
        ): String {
            val buildType = if (enableMatchDebugVariant) BUILD_TYPE_DEBUG else variant.buildType.orEmpty()
            val variantPath = listOfNotNull(variant.flavorName?.ifEmpty { null }, buildType)
                .joinToString(separator = "/")
            return project.layout.buildDirectory.dir("outputs/apk/$variantPath").get().asFile.path
        }

        private fun artifactView(configuration: Configuration, artifactType: String): ArtifactView =
            configuration.incoming.artifactView { view ->
                view.isLenient = true
                view.attributes.attribute(ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE, artifactType)
            }

        private fun ArtifactView.entries(artifactType: String): Provider<List<ArchiveDependency>> =
            artifacts.resolvedArtifacts.map { results ->
                results.mapNotNull { result -> result.toArchiveDependency(artifactType) }
            }
    }
}

/**
 * Maps a resolved artifact to the archive model written to dependencies.json.
 * Project components become module entries, external components library entries;
 * anything else (for example flat-file dependencies) is not attributable and is skipped.
 */
internal fun ResolvedArtifactResult.toArchiveDependency(artifactType: String): ArchiveDependency? =
    toArchiveDependency(id.componentIdentifier, file, artifactType)

internal fun toArchiveDependency(
    componentId: ComponentIdentifier,
    file: File,
    artifactType: String,
): ArchiveDependency? = when (componentId) {
    is ProjectComponentIdentifier -> when (artifactType) {
        ARTIFACT_TYPE_AAR -> ModuleDependency(
            name = componentId.projectPath.trim(':'),
            pathToArtifact = file.path,
        )

        else -> JavaModuleDependency(
            name = componentId.projectPath.trim(':'),
            pathToArtifact = file.path,
        )
    }

    is ModuleComponentIdentifier -> ExternalDependency(
        name = componentId.displayName,
        pathToArtifact = file.path,
    )

    else -> null
}

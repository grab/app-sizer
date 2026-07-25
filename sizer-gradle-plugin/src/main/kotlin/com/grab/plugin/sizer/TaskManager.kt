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

package com.grab.plugin.sizer

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.ApplicationVariant
import com.grab.plugin.sizer.configuration.DefaultVariantFilter
import com.grab.plugin.sizer.dependencies.VariantArtifacts
import com.grab.plugin.sizer.tasks.AppSizeAnalysisTask
import com.grab.plugin.sizer.tasks.GenerateApkTask
import com.grab.plugin.sizer.tasks.GenerateArchivesListTask
import com.grab.plugin.sizer.utils.ANDROID_APPLICATION_PLUGIN
import com.grab.plugin.sizer.utils.PluginLogger
import org.gradle.api.Project

/*
 * This internal `TaskManager` class is used for configuring tasks for the Plugin
 *
 * The `TaskManager` class is responsible for setting up tasks based on plugin extensions.
 * It evaluates and applies tasks to projects based on the configuration found in a provided `AppSizePluginExtension`.
 */
internal class TaskManager(
    private val project: Project,
    private val pluginExtension: AppSizePluginExtension,
    private val logger: PluginLogger
) {
    fun configTasks() {
        project.pluginManager.withPlugin(ANDROID_APPLICATION_PLUGIN) {
            val androidComponents =
                project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants { variant ->
                // onVariants callbacks run after the build script is evaluated,
                // so the appSizer extension is fully configured at this point
                if (pluginExtension.enabled) {
                    configAppSizeTask(project, variant)
                }
            }
        }
    }

    private fun configAppSizeTask(project: Project, variant: ApplicationVariant) {
        val variantFilter = DefaultVariantFilter(variant)
        pluginExtension.input.variantFilter?.execute(variantFilter)
        if (variantFilter.ignored) return

        val android = project.extensions.getByType(ApplicationExtension::class.java)

        /*
         * All archives contributing to the variant are resolved from the variant's runtime
         * classpath. The resulting file collection carries the task dependencies that build
         * the module AARs/JARs, so no manual cross-project task wiring is needed.
         */
        val variantArtifacts = VariantArtifacts.create(
            project = project,
            variant = variant,
            android = android,
            enableMatchDebugVariant = pluginExtension.input.enableMatchDebugVariant,
            logger = logger,
        )

        val generateApkTask = GenerateApkTask.registerTask(
            project,
            pluginExtension,
            variant,
            android
        )

        val generateArchivesListTask = GenerateArchivesListTask.registerTask(
            project,
            variantName = variant.name,
            variantArtifacts = variantArtifacts,
        )

        AppSizeAnalysisTask.registerTask(
            project,
            variant,
            pluginExtension,
            generateApkTask,
            generateArchivesListTask,
            archiveFiles = variantArtifacts.archiveFiles,
        )
    }
}

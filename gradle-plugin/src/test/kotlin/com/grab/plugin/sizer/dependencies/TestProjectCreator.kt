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
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder

object TestProjectCreator {

    fun createRootProject(): Project {
        return ProjectBuilder.builder().build()
    }

    fun createAndroidLibraryProject(rootProject: Project, name: String, flavors: List<String> = listOf("flavor1", "flavor2")): Project {
        val project = ProjectBuilder.builder().withName(name).withParent(rootProject).build()
        project.pluginManager.apply(LibraryPlugin::class.java)

        val android = project.extensions.getByType(LibraryExtension::class.java)
        android.compileSdkVersion(30)
        android.namespace = "com.example.${project.name}"
        android.defaultConfig {
            minSdk = 21
            targetSdk = 30
        }
        android.buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
            }
            getByName("release") {
                isMinifyEnabled = true
            }
        }
        if (flavors.isNotEmpty()) {
            android.flavorDimensions("version")
            android.productFlavors {
                flavors.forEach { flavor ->
                    create(flavor) {
                        dimension = "version"
                    }
                }
            }
        }

        project.doEvaluate()
        return project
    }

    fun createAndroidAppProject(rootProject: Project, name: String): Project {
        val project = ProjectBuilder.builder().withName(name).withParent(rootProject).build()
        project.pluginManager.apply(AppPlugin::class.java)

        val android = project.extensions.getByType(AppExtension::class.java)
        configureAndroidAppExtension(android, project.name)

        project.doEvaluate()
        return project
    }

    fun createJavaProject(rootProject: Project, name: String): Project {
        val project = ProjectBuilder.builder().withName(name).withParent(rootProject).build()
        project.pluginManager.apply(JavaPlugin::class.java)
        project.doEvaluate()
        return project
    }

    private fun configureAndroidAppExtension(android: AppExtension, projectName: String) {
        android.compileSdkVersion(30)
        android.namespace = "com.example.$projectName"
        android.defaultConfig {
            applicationId = "com.example.$projectName"
            minSdkVersion(21)
            targetSdkVersion(30)
            versionCode = 1
            versionName = "1.0"
        }

        android.buildTypes {
            getByName("debug") {
                isMinifyEnabled = false
            }
            getByName("release") {
                isMinifyEnabled = true
            }
        }
        android.flavorDimensions("version")
        android.productFlavors {
            create("flavor1") {
                dimension = "version"
            }
            create("flavor2") {
                dimension = "version"
            }
        }
    }
}

/**
 * Forces an evaluation of the project thereby running all configurations
 */
fun Project.doEvaluate() {
    getTasksByName("tasks", false)
    (this as DefaultProject).evaluate()
}
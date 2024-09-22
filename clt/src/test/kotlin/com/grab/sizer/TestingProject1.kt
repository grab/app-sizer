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

package com.grab.sizer

import com.grab.sizer.config.ApkGenerationConfig
import com.grab.sizer.config.Config
import com.grab.sizer.config.ProjectInputConfig
import com.grab.sizer.config.ReportConfig
import com.grab.sizer.utils.SizerInputFile
import java.io.File

internal const val APP_APK = "app.apk"
internal const val MODULE1_AAR = "module1-debug.aar"
internal const val MODULE2_AAR = "module2-debug.aar"
internal const val JAVA_MODULE_JAR = "java-module.jar"
internal const val NOT_A_MODULE_AAR = "not-a-module.aar"
internal const val NOT_A_JAVA_MODULE_JAR = "not-a-java-module.jar"
internal const val BUILD_GRADLE = "build.gradle"
internal const val SECURITY_CRYPTO_SOURCES_JAR = "security-crypto-1.1.0-alpha03-sources.jar"
internal const val SECURITY_CRYPTO_POM = "security-crypto-1.1.0-alpha03.pom"
internal const val SECURITY_CRYPTO_AAR = "security-crypto-1.1.0-alpha03.aar"
internal const val WORK_MULTIPROCESS_SOURCES_JAR = "work-multiprocess-2.8.0-sources.jar"
internal const val WORK_MULTIPROCESS_AAR = "work-multiprocess-2.8.0.aar"
internal const val WORK_MULTIPROCESS_POM = "work-multiprocess-2.8.0.pom"

/**
 * This class contain a project files & folders for testing the [CltInputProvider]
 * It build a project with this structure:
 * ```
 * ./user-folder/root-project
 *  - app
 *  - module1
 *  - group1:
 *      - module2
 *      - java-module
 * ```
 * Beside that there is two trash folders which are not project module but contains trash aar & jar files
 * - ./user-folder/root-project/group1/not-a-module
 * - ./user-folder/root-project/group1/not-java-module
 */
class TestingProject1 : FileSystem {
    val projectDir: FakeFile = createProjectDir()
    val libDir: FakeFile = createLibDir()
    val allFiles = projectDir.getAll() + libDir.getAll()
    val config = createConfig()

    /**
     * When config.projectInput.modulesDirIsProjectRoot = false
     */
    val expectingAllAarsWhenNotAProjectRoot = projectDir.getAll()
        .filter { it.extension == EXT_AAR }
        .map { SizerInputFile(file = it, tag = it.nameWithoutExtension) }

    /**
     * When config.projectInput.modulesDirIsProjectRoot = false
     */
    val expectingAllJarsWhenNotAProjectRoot = projectDir.getAll().filter { it.extension == EXT_JAR }
        .map { SizerInputFile(file = it, tag = it.nameWithoutExtension) }

    val expectingAllAars = projectDir.getAll()
        .filter { it.extension == EXT_AAR }
        .map {
            when (it.name) {
                MODULE1_AAR -> SizerInputFile(
                    file = it,
                    tag = "module1"
                )

                MODULE2_AAR -> SizerInputFile(
                    file = it,
                    tag = "group1:module2"
                )

                else -> SizerInputFile(file = it, tag = it.nameWithoutExtension)
            }
        }

    val expectingAllJars = projectDir.getAll().filter { it.extension == EXT_JAR }
        .map {
            when (it.name) {
                JAVA_MODULE_JAR -> SizerInputFile(
                    file = it,
                    tag = "group1:java-module"
                )

                else -> SizerInputFile(file = it, tag = it.nameWithoutExtension)
            }
        }


    val expectingModuleAars = expectingAllAars.filter { it.file.name != NOT_A_MODULE_AAR }
    val expectingModuleJars = expectingAllJars.filter { it.file.name != NOT_A_JAVA_MODULE_JAR }

    val expectingLibAars = libDir.getAll().filter { it.extension == EXT_AAR }
        .map {
            SizerInputFile(
                file = it,
                tag = it.nameWithoutExtension
            )
        }
    val expectingLibJars = libDir.getAll().filter { it.extension == EXT_JAR }
        .map {
            SizerInputFile(
                file = it,
                tag = it.nameWithoutExtension
            )
        }


    private fun createConfig(): Config {
        return Config(
            projectInput = ProjectInputConfig(
                version = "0.0.1",
                projectName = "testing01",
                modulesDirectory = projectDir,
                projectRoot = projectDir,
                librariesDirectory = libDir
            ),
            apkGeneration = ApkGenerationConfig(
                bundleToolPath = "bundle/path",
                appBundleFilePath = "app/bundle/bundle.aab",
                deviceSpecPaths = emptyList(),
                keySigning = null
            ),
            report = ReportConfig(
                outputDirectoryPath = "output",
                customAttributes = null,
                influxDbConfig = null
            )
        )
    }

    private fun createLibDir(): FakeFile {
        return FakeFile(File("."), "gradle-cache", directory = true) {
            addDirectory("androidx.security") {
                addDirectory("security-crypto") {
                    addDirectory("1.1.0-alpha03") {
                        addDirectory("a96855861b33f9a46ca6a1556118ae592cad2014") {
                            addFile(SECURITY_CRYPTO_SOURCES_JAR)
                        }
                        addDirectory("b3c8960986915ab431476ae2072273adb4b83515") {
                            addFile(SECURITY_CRYPTO_POM)
                        }
                        addDirectory("f54110eab7610d08d7c41c594b3a248dac488e00") {
                            addFile(SECURITY_CRYPTO_AAR)
                        }
                    }
                }
            }

            addDirectory("androidx.work") {
                addDirectory("work-multiprocess") {
                    addDirectory("2.8.0") {
                        addDirectory("8547c508168f54ce7c2fa0c4b6c3fc8850d30f23") {
                            addFile(WORK_MULTIPROCESS_SOURCES_JAR)
                        }
                        addDirectory("90aacad73ba44fe05b25de0c5308160c703dba0b") {
                            addFile(WORK_MULTIPROCESS_AAR)
                        }
                        addDirectory("77a1c6094184a05d8718a77f004aaa75fd296b") {
                            addFile(WORK_MULTIPROCESS_POM)
                        }
                    }
                }
            }
        }
    }

    private fun createProjectDir(): FakeFile {
        val projectParent = FakeFile(File("."), "user-folder") {
            addDirectory("root-project") {
                addFile(BUILD_GRADLE)
                addDirectory("app") {
                    addFile(BUILD_GRADLE)
                    addDirectory("build") {
                        addDirectory("outputs") {
                            addDirectory("apk") {
                                addDirectory("debug") {
                                    addFile(APP_APK)
                                }
                            }
                        }
                    }
                }
                addDirectory("module1") {
                    addFile(BUILD_GRADLE)
                    addDirectory("build") {
                        addDirectory("outputs") {
                            addDirectory("aar") {
                                addFile(MODULE1_AAR)
                            }
                        }
                    }
                }
                addDirectory("group1") {
                    addDirectory("module2") {
                        addFile(BUILD_GRADLE)
                        addDirectory("build") {
                            addDirectory("outputs") {
                                addDirectory("aar") {
                                    addFile(MODULE2_AAR)
                                }
                            }
                        }
                    }

                    addDirectory("java-module") {
                        addFile(BUILD_GRADLE)
                        addDirectory("build") {
                            addDirectory("libs") {
                                addFile(JAVA_MODULE_JAR)
                            }
                        }
                    }

                    addDirectory("not-a-module") {
                        addDirectory("build") {
                            addDirectory("outputs") {
                                addDirectory("aar") {
                                    addFile(NOT_A_MODULE_AAR)
                                }
                            }
                        }
                    }

                    addDirectory("not-java-module") {
                        addDirectory("build") {
                            addDirectory("libs") {
                                addFile(NOT_A_JAVA_MODULE_JAR)
                            }
                        }
                    }
                }
            }
        }
        return projectParent.children.first()
    }

    override fun create(parent: File, path: String): File {
        val file = File(parent, path)
        return allFiles.find { it.path == file.path } ?: file
    }
}

class FakeFile(
    val parent: File,
    path: String,
    val directory: Boolean = false,
    addChild: FakeFile.() -> Unit = {}
) : File(parent, path) {
    val children: MutableList<FakeFile> = mutableListOf()

    init {
        addChild()
    }

    override fun getParentFile(): File = parent
    fun addDirectory(name: String, addChild: FakeFile.() -> Unit = {}) {
        children.add(
            FakeFile(this, name, true).also { it.addChild() }
        )
    }

    override fun exists(): Boolean = true

    fun addFile(name: String) {
        children.add(FakeFile(this, name, false))
    }

    override fun listFiles(): Array<File> = children.toTypedArray()
    override fun isDirectory(): Boolean = directory
    override fun isFile(): Boolean = !directory
    override fun createNewFile(): Boolean = true
    override fun mkdirs(): Boolean = true
    override fun mkdir(): Boolean = true

    fun getAll(): List<File> = walk().toList()

}
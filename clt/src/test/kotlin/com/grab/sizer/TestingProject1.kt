package com.grab.sizer

import com.grab.sizer.config.ApkGenerationConfig
import com.grab.sizer.config.Config
import com.grab.sizer.config.ProjectInputConfig
import com.grab.sizer.config.ReportConfig
import java.io.File

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

    val expectingAllAars = projectDir.getAll().filter { it.extension == EXT_AAR }
    val expectingAllJars = projectDir.getAll().filter { it.extension == EXT_JAR}

    val expectingModuleAars = expectingAllAars.filter { it.name != "not-a-module.aar" }
    val expectingModuleJars = expectingAllJars.filter { it.name != "not-a-java-module.jar" }

    val expectingLibAars = libDir.getAll().filter { it.extension == EXT_AAR }
    val expectingLibJars = libDir.getAll().filter { it.extension == EXT_JAR }


    private fun createConfig(): Config {
        return Config(
            projectInput = ProjectInputConfig(
                version = "0.0.1",
                projectName = "testing01",
                modulesDirectory = projectDir,
                modulesDirIsProjectRoot = true,
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
                            addFile("security-crypto-1.1.0-alpha03-sources.jar")
                        }
                        addDirectory("b3c8960986915ab431476ae2072273adb4b83515") {
                            addFile("security-crypto-1.1.0-alpha03.pom")
                        }
                        addDirectory("f54110eab7610d08d7c41c594b3a248dac488e00") {
                            addFile("security-crypto-1.1.0-alpha03.aar")
                        }
                    }
                }
            }

            addDirectory("androidx.work") {
                addDirectory("work-multiprocess") {
                    addDirectory("2.8.0") {
                        addDirectory("8547c508168f54ce7c2fa0c4b6c3fc8850d30f23") {
                            addFile("work-multiprocess-2.8.0-sources.jar")
                        }
                        addDirectory("90aacad73ba44fe05b25de0c5308160c703dba0b") {
                            addFile("work-multiprocess-2.8.0.aar")
                        }
                        addDirectory("77a1c6094184a05d8718a77f004aaa75fd296b") {
                            addFile("work-multiprocess-2.8.0.pom")
                        }
                    }
                }
            }
        }
    }

    private fun createProjectDir(): FakeFile {
        val projectParent = FakeFile(File("."), "user-folder") {
            addDirectory("root-project") {
                addFile("build.gradle")
                addDirectory("app") {
                    addFile("build.gradle")
                    addDirectory("build") {
                        addDirectory("outputs") {
                            addDirectory("apk") {
                                addDirectory("debug") {
                                    addFile("app.apk")
                                }
                            }
                        }
                    }
                }
                addDirectory("module1") {
                    addFile("build.gradle")
                    addDirectory("build") {
                        addDirectory("outputs") {
                            addDirectory("aar") {
                                addFile("module1.aar")
                            }
                        }
                    }
                }
                addDirectory("group1") {
                    addDirectory("module2") {
                        addFile("build.gradle")
                        addDirectory("build") {
                            addDirectory("outputs") {
                                addDirectory("aar") {
                                    addFile("module2.aar")
                                }
                            }
                        }
                    }

                    addDirectory("java-module") {
                        addFile("build.gradle")
                        addDirectory("build") {
                            addDirectory("libs") {
                                addFile("java-module.jar")
                            }
                        }
                    }

                    addDirectory("not-a-module") {
                        addDirectory("build") {
                            addDirectory("outputs") {
                                addDirectory("aar") {
                                    addFile("not-a-module.aar")
                                }
                            }
                        }
                    }

                    addDirectory("not-java-module") {
                        addDirectory("build") {
                            addDirectory("libs") {
                                addFile("not-a-java-module.jar")
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
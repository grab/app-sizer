package com.grab.plugin.size.dependencies


interface ArchiveDependency {
    val name: String
    val pathToArtifact: String
}

data class ExternalDependency(
    override val name: String,
    override val pathToArtifact: String,
    val group: String,
    val version: String
) : ArchiveDependency

data class ModuleDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency

data class JavaModuleDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency
data class AppDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency
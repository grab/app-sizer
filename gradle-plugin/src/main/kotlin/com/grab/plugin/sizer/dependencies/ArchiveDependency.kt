package com.grab.plugin.sizer.dependencies


interface ArchiveDependency {
    val name: String
    val pathToArtifact: String
    val id: String
}

data class ExternalDependency(
    override val name: String,
    override val pathToArtifact: String
) : ArchiveDependency {
    override val id: String
        get() = name

    override fun hashCode(): Int = id.hashCode()
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ExternalDependency

        return id == other.id
    }
}

data class ModuleDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency {
    override val id: String
        get() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModuleDependency

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class JavaModuleDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency {
    override val id: String
        get() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JavaModuleDependency

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

data class AppDependency(override val name: String, override val pathToArtifact: String) : ArchiveDependency {
    override val id: String
        get() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppDependency

        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
package com.grab.sizer.analyzer

import org.yaml.snakeyaml.Yaml
import java.io.File


/**
 * An interface that represents a bi-directional mapping between modules and teams.
 * It allows the retrieval of the associated team for a given module and vice versa.
 */
interface TeamMapping {
    val teamToModuleMap: Map<String, List<String>>
    val moduleToTeamMap: Map<String, String>
}

class DummyTeamMapping : TeamMapping {
    override val teamToModuleMap: Map<String, List<String>> = emptyMap()
    override val moduleToTeamMap: Map<String, String> = emptyMap()
}

/**
 * Generates TeamMapping data from a YAML file. It provides efficient, lazy access to both
 * team-to-module and module-to-team mappings.
 *
 * @property ymlFile The YAML file that contains the mapping information.
 */
class YmlTeamMapping(
    private val ymlFile: File
) : TeamMapping {
    override val teamToModuleMap: Map<String, List<String>> by lazy {
        loadTeamToModuleMap()
    }
    override val moduleToTeamMap: Map<String, String> by lazy {
        mutableMapOf<String, String>().apply {
            teamToModuleMap.forEach { (team, modules) ->
                modules.forEach { put(it, team) }
            }
        }
    }

    private fun loadTeamToModuleMap(): Map<String, List<String>> = Yaml().load(ymlFile.inputStream())
}
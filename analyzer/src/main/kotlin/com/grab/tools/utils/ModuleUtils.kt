package com.grab.tools.utils

import com.grab.tools.model.Contributor
import com.grab.tools.report.Module
import com.grab.tools.report.moduleToContributors

object ModuleUtils {

    fun mapContributorToModule(list : Set<Contributor>): List<Module> {
        val moduleToContributorMap = list.moduleToContributors().toMutableMap()
        val features = moduleToContributorMap.map {
            Module(it.key, it.value)
        }
        return features
    }
}
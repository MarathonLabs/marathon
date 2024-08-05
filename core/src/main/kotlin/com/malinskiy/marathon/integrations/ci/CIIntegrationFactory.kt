package com.malinskiy.marathon.integrations.ci

import com.malinskiy.marathon.config.CIConfiguration
import com.malinskiy.marathon.config.Configuration

object CIIntegrationFactory {
    fun get(configuration: Configuration) = when (configuration.ciConfiguration) {
        CIConfiguration.Auto -> auto()
        CIConfiguration.None -> None
        CIConfiguration.Teamcity -> Teamcity
    }

    private fun auto() = when {
        // Teamcity predefined variables: https://www.jetbrains.com/help/teamcity/predefined-build-parameters.html#84e0b866
        System.getenv().containsValue("TEAMCITY_VERSION") -> Teamcity
        else -> None
    }
}

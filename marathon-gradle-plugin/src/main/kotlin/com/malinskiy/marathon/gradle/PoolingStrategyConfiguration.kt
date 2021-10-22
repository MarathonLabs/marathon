package com.malinskiy.marathon.gradle

class PoolingStrategyConfiguration {
    var operatingSystem: Boolean? = null
    var abi: Boolean? = null
    var manufacturer: Boolean? = null
    var model: Boolean? = null
}

fun PoolingStrategyConfiguration.toStrategy(): com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration {
    if (listOf(operatingSystem, abi, manufacturer, model).all { it == null || it == false }) {
        return com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration
    } else {
        val strategies = mutableListOf<com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration>()
        when {
            operatingSystem != null && operatingSystem == true -> {
                strategies.add(com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.OperatingSystemVersionPoolingStrategyConfiguration)
            }
            abi != null && abi == true -> {
                strategies.add(com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.AbiPoolingStrategyConfiguration)
            }
            manufacturer != null && manufacturer == true -> {
                strategies.add(com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.ManufacturerPoolingStrategyConfiguration)
            }
            model != null && model == true -> {
                strategies.add(com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration)
            }
        }
        return com.malinskiy.marathon.config.strategy.PoolingStrategyConfiguration.ComboPoolingStrategyConfiguration(strategies)
    }
}

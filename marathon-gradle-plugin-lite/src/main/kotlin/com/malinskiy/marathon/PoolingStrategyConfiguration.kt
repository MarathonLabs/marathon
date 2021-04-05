package com.malinskiy.marathon

import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ComboPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy

class PoolingStrategyConfiguration {
    var operatingSystem: Boolean? = null
    var abi: Boolean? = null
    var manufacturer: Boolean? = null
    var model: Boolean? = null
}

fun PoolingStrategyConfiguration.toStrategy(): PoolingStrategy {
    if (listOf(operatingSystem, abi, manufacturer, model).all { it == null || it == false }) {
        return OmniPoolingStrategy()
    } else {
        val strategies = mutableListOf<PoolingStrategy>()
        when {
            operatingSystem != null && operatingSystem == true -> {
                strategies.add(OperatingSystemVersionPoolingStrategy())
            }
            abi != null && abi == true -> {
                strategies.add(AbiPoolingStrategy())
            }
            manufacturer != null && manufacturer == true -> {
                strategies.add(ManufacturerPoolingStrategy())
            }
            model != null && model == true -> {
                strategies.add(ModelPoolingStrategy())
            }
        }
        return ComboPoolingStrategy(strategies)
    }
}

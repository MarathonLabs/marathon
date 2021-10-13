package com.malinskiy.marathon.config.strategy

sealed class PoolingStrategyConfiguration {
    object OmniPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object AbiPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object ManufacturerPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object ModelPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object OperatingSystemVersionPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    data class ComboPoolingStrategyConfiguration(val list: List<PoolingStrategyConfiguration>) : PoolingStrategyConfiguration()
}

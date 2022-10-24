package com.malinskiy.marathon.config.strategy

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.OmniPoolingStrategyConfiguration::class, name = "omni"),
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.AbiPoolingStrategyConfiguration::class, name = "abi"),
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.ManufacturerPoolingStrategyConfiguration::class, name = "manufacturer"),
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.ModelPoolingStrategyConfiguration::class, name = "device-model"),
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.OperatingSystemVersionPoolingStrategyConfiguration::class, name = "os-version"),
    JsonSubTypes.Type(value = PoolingStrategyConfiguration.ComboPoolingStrategyConfiguration::class, name = "combo"),
)
sealed class PoolingStrategyConfiguration : Serializable {
    object OmniPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object AbiPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object ManufacturerPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object ModelPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    object OperatingSystemVersionPoolingStrategyConfiguration : PoolingStrategyConfiguration()
    data class ComboPoolingStrategyConfiguration(val list: List<PoolingStrategyConfiguration>) : PoolingStrategyConfiguration()
}

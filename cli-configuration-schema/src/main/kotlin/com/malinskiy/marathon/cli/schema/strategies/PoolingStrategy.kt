package com.malinskiy.marathon.cli.schema.strategies

sealed class PoolingStrategy {
    object Omni : PoolingStrategy()
    object ABI : PoolingStrategy()
    data class Combo(val list: List<PoolingStrategy>) : PoolingStrategy()
    object Manufacturer : PoolingStrategy()
    object Model : PoolingStrategy()
    object OperatingSystem : PoolingStrategy()
}

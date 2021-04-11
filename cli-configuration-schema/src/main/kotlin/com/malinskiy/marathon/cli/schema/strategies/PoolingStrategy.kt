package com.malinskiy.marathon.cli.schema.strategies

import java.io.Serializable

sealed class PoolingStrategy: Serializable {
    object Omni : PoolingStrategy()
    object ABI : PoolingStrategy()
    data class Combo(val list: List<PoolingStrategy>) : PoolingStrategy()
    object Manufacturer : PoolingStrategy()
    object Model : PoolingStrategy()
    object OsVersion : PoolingStrategy()
}

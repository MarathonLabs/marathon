package com.malinskiy.marathon.lite.configuration.strategies

import java.io.Serializable

sealed class PoolingStrategy: Serializable {
    object Omni : PoolingStrategy()
    object ABI : PoolingStrategy()
    data class Combo(val list: List<PoolingStrategy>) : PoolingStrategy()
    object Manufacturer : PoolingStrategy()
    object Model : PoolingStrategy()
    object OsVersion : PoolingStrategy()
}

package com.malinskiy.marathon.lite.configuration

import java.io.Serializable

data class FilteringConfiguration(
    val allowList: List<TestFilter> = emptyList(),
    val blockList: List<TestFilter> = emptyList()
) : Serializable


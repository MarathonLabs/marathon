package com.malinskiy.marathon.cli.schema

data class FilteringConfiguration(
    val allowList: List<TestFilter> = emptyList(),
    val blockList: List<TestFilter> = emptyList()
)


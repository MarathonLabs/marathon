package com.malinskiy.marathon.execution

data class FilteringConfiguration(
    val allowlist: List<TestFilter> = emptyList(),
    val blocklist: List<TestFilter> = emptyList()
)

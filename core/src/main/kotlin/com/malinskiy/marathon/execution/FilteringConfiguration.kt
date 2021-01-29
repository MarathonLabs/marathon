package com.malinskiy.marathon.execution

data class FilteringConfiguration(
    val allowlist: Collection<TestFilter> = emptyList(),
    val blocklist: Collection<TestFilter> = emptyList()
)

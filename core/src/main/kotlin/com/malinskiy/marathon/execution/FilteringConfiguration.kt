package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty

data class FilteringConfiguration(
    @JsonProperty("allowlist", required = false) val allowList: Collection<TestFilter> = emptyList(),
    @JsonProperty("blocklist", required = false) val blockList: Collection<TestFilter> = emptyList()
)

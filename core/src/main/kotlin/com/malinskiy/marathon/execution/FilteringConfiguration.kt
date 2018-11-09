package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty

data class FilteringConfiguration(@JsonProperty("whitelist", required = false) val whitelist: Collection<TestFilter> = emptyList(),
                                  @JsonProperty("blacklist", required = false) val blacklist: Collection<TestFilter> = emptyList())

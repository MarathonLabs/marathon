package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty

data class FilteringConfiguration(@JsonProperty("whitelist") val whitelist: Collection<TestFilter>,
                                  @JsonProperty("blacklist") val blacklist: Collection<TestFilter>)

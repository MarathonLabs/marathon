package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonProperty

enum class TestType(val value: String) {
    @JsonProperty("xcuitest") XCUITEST("xcuitest"),
    @JsonProperty("xctest") XCTEST("xctest"),
    @JsonProperty("logic_test") LOGIC_TEST("logic_test"),
}

package com.malinskiy.marathon.config.vendor.ios

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TestParserConfiguration.NmTestParserConfiguration::class, name = "nm"),
    JsonSubTypes.Type(value = TestParserConfiguration.XCTestParserConfiguration::class, name = "xctest"),
)
sealed class TestParserConfiguration {
    object NmTestParserConfiguration : TestParserConfiguration()
    object XCTestParserConfiguration : TestParserConfiguration()
}

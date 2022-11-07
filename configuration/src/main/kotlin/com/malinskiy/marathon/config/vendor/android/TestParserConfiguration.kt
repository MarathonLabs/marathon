package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TestParserConfiguration.LocalTestParserConfiguration::class, name = "local"),
    JsonSubTypes.Type(value = TestParserConfiguration.RemoteTestParserConfiguration::class, name = "remote"),
)
sealed class TestParserConfiguration {
    object LocalTestParserConfiguration : TestParserConfiguration()
    data class RemoteTestParserConfiguration(
        val instrumentationArgs: Map<String, String> = emptyMap(),
    ) : TestParserConfiguration()
}

package com.malinskiy.marathon.config.vendor.android

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.Serializable


@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = TestParserConfiguration.LocalTestParserConfiguration::class, name = "local"),
    JsonSubTypes.Type(value = TestParserConfiguration.RemoteTestParserConfiguration::class, name = "remote"),
)
sealed class TestParserConfiguration : Serializable {
    object LocalTestParserConfiguration : TestParserConfiguration()
    data class RemoteTestParserConfiguration(
        val instrumentationArgs: Map<String, String> = emptyMap(),
    ) : TestParserConfiguration()
}

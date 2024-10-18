package com.malinskiy.marathon.config.vendor.apple

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.io.File

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = DeviceProvider.Static::class, names = arrayOf("static", "marathondevices")),
    JsonSubTypes.Type(value = DeviceProvider.Dynamic::class, name = "dynamic"),
)
sealed class DeviceProvider {
    data class Static(
        @JsonProperty("path") val path: File? = null
    ) : DeviceProvider()

    data class Dynamic(
        @JsonProperty("host") val host: String = "127.0.0.1",
        @JsonProperty("port") val port: Int = 5037,
    ) : DeviceProvider()
}

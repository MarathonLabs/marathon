package com.malinskiy.marathon.apple.configuration

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.malinskiy.marathon.config.vendor.apple.SshAuthentication

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Transport.Local::class, name = "local"),
    JsonSubTypes.Type(value = Transport.Ssh::class, name = "ssh"),
)
sealed interface Transport {
    object Local : Transport

    data class Ssh(
        @JsonProperty("addr") val addr: String,
        @JsonProperty("port") val port: Int = 22,
        @JsonProperty("authentication") val authentication: SshAuthentication? = null,
        @JsonProperty("checkReachability") val checkReachability: Boolean = true,
    ) : Transport
}

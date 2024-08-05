package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = CIConfiguration.Auto::class, name = "auto"),
    JsonSubTypes.Type(value = CIConfiguration.None::class, name = "none"),
    JsonSubTypes.Type(value = CIConfiguration.Teamcity::class, name = "teamcity"),
)
sealed class CIConfiguration {
    object Auto : CIConfiguration()
    object None : CIConfiguration()
    object Teamcity : CIConfiguration()
}

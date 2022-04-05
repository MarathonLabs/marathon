package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = BuildManagementConfiguration.TeamCityConfiguration::class, name = "teamcity"),
)
sealed class BuildManagementConfiguration {
    object TeamCityConfiguration : BuildManagementConfiguration()
    object NoBuildManagementConfiguration: BuildManagementConfiguration()
}


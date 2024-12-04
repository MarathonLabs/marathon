package com.malinskiy.marathon.config

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ProgressConfiguration.Auto::class, name = "auto"),
    JsonSubTypes.Type(value = ProgressConfiguration.Custom::class, name = "custom"),
)

sealed class ProgressConfiguration {
    data object Auto : ProgressConfiguration()
    data class Custom(val values: List<ProgressReporterConfiguration>) : ProgressConfiguration()
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Stdout::class, name = "stdout"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Teamcity::class, name = "teamcity"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Device::class, name = "device"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Billing::class, name = "billing"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.JUnit::class, name = "junit"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Timeline::class, name = "timeline"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Raw::class, name = "raw"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Allure::class, name = "allure"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.Html::class, name = "html"),
    JsonSubTypes.Type(value = ProgressReporterConfiguration.ApiV1::class, name = "api_v1"),
)
sealed class ProgressReporterConfiguration {
    data object Stdout : ProgressReporterConfiguration()
    data object Teamcity : ProgressReporterConfiguration()
    data object Device : ProgressReporterConfiguration()
    data object Billing : ProgressReporterConfiguration()
    data object JUnit : ProgressReporterConfiguration()
    data object Timeline : ProgressReporterConfiguration()
    data object Raw : ProgressReporterConfiguration()
    data object Test : ProgressReporterConfiguration()
    data object Allure : ProgressReporterConfiguration()
    data object Html : ProgressReporterConfiguration()
    data class ApiV1(val target: String, val run_id: String) : ProgressReporterConfiguration()
}

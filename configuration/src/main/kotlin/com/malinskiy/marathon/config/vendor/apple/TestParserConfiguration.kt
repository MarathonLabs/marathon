package com.malinskiy.marathon.config.vendor.apple

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
    data class NmTestParserConfiguration(val testClassRegexes: Collection<Regex> = listOf(Regex("^((?!Abstract).)*Test[s]*$"))) : TestParserConfiguration() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NmTestParserConfiguration

            //For testing we need to compare configuration instances. Unfortunately Regex equality is broken so need to map it to String
            return testClassRegexes.map { it.pattern } == other.testClassRegexes.map { it.pattern }
        }

        override fun hashCode(): Int {
            return testClassRegexes.hashCode()
        }
    }
    object XCTestParserConfiguration : TestParserConfiguration()
}

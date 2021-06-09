package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

data class AnnotationDataFilter(
    @JsonProperty("nameRegex") val nameRegex: Regex,
    @JsonProperty("valueRegex") val valueRegex: Regex
) : TestFilter {
    override fun validate() = Unit

    override fun filter(tests: List<Test>): List<Test> = tests.filter { test ->
        test.metaProperties.any {
            match(it)
        }
    }

    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { test ->
        test.metaProperties.any {
            match(it)
        }
    }

    private fun match(metaProperty: MetaProperty): Boolean {
        return nameRegex.matches(metaProperty.name) && metaProperty.values.containsKey("value") && valueRegex.matches(metaProperty.values["value"].toString())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AnnotationDataFilter) return false
        return (nameRegex.toString() + valueRegex.toString()).contentEquals((other.nameRegex.toString() + other.valueRegex.toString()))
    }

    override fun hashCode(): Int = nameRegex.hashCode() + valueRegex.hashCode()

    override fun toString(): String {
        return "AnnotationDataFilter(nameRegex=$nameRegex, valuesRegex=$valueRegex)"
    }
}

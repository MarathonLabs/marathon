package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

data class AnnotationDataFilter(val cnf: TestFilterConfiguration.AnnotationDataFilterConfiguration) : TestFilter {
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
        return cnf.nameRegex.matches(metaProperty.name) && metaProperty.values.containsKey("value") && cnf.valueRegex.matches(metaProperty.values["value"].toString())
    }

    override fun equals(other: Any?): Boolean {
        if (other !is AnnotationDataFilter) return false
        return (cnf.nameRegex.toString() + cnf.valueRegex.toString()).contentEquals((other.cnf.nameRegex.toString() + other.cnf.valueRegex.toString()))
    }

    override fun hashCode(): Int = cnf.nameRegex.hashCode() + cnf.valueRegex.hashCode()

    override fun toString(): String {
        return "AnnotationDataFilter(nameRegex=${cnf.nameRegex}, valuesRegex=${cnf.valueRegex})"
    }
}

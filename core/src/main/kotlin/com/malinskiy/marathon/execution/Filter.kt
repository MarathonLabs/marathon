package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.test.Test

interface TestFilter {
    fun filter(tests: List<Test>): List<Test>
    fun filterNot(tests: List<Test>): List<Test>
}

data class SimpleClassnameFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches(it.clazz) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches(it.clazz) }

    override fun equals(other: Any?): Boolean {
        if(other !is SimpleClassnameFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }
}

data class FullyQualifiedClassnameFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches("${it.pkg}.${it.clazz}") }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches("${it.pkg}.${it.clazz}") }

    override fun equals(other: Any?): Boolean {
        if(other !is FullyQualifiedClassnameFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }
}

data class TestPackageFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches("${it.pkg}") }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches("${it.pkg}") }

    override fun equals(other: Any?): Boolean {
        if(other !is TestPackageFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }
}

data class AnnotationFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { it.annotations.any { regex.matches("$it") } }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { it.annotations.any { regex.matches("$it") } }

    override fun equals(other: Any?): Boolean {
        if(other !is AnnotationFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }
}

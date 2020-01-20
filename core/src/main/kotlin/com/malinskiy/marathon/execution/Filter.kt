package com.malinskiy.marathon.execution

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.test.Test

interface TestFilter {
    fun filter(tests: List<Test>): List<Test>
    fun filterNot(tests: List<Test>): List<Test>
}

fun TestFilter.matches(test: Test): Boolean =
    filter(listOf(test)).isNotEmpty()

fun Collection<TestFilter>.matches(test: Test): Boolean =
    isNotEmpty() && all { it.matches(test) }

data class SimpleClassnameFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches(it.clazz) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches(it.clazz) }

    override fun equals(other: Any?): Boolean {
        if (other !is SimpleClassnameFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = regex.hashCode()

    override fun toString(): String {
        return "SimpleClassnameFilter(regex=$regex)"
    }
}

data class FullyQualifiedClassnameFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches("${it.pkg}.${it.clazz}") }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches("${it.pkg}.${it.clazz}") }

    override fun equals(other: Any?): Boolean {
        if (other !is FullyQualifiedClassnameFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = regex.hashCode()

    override fun toString(): String {
        return "FullyQualifiedClassnameFilter(regex=$regex)"
    }
}

data class TestPackageFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches(it.pkg) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches(it.pkg) }

    override fun equals(other: Any?): Boolean {
        if (other !is TestPackageFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = regex.hashCode()

    override fun toString(): String {
        return "TestPackageFilter(regex=$regex)"
    }
}

data class AnnotationFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { it.metaProperties.map { it.name }.any(regex::matches) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { it.metaProperties.map { it.name }.any(regex::matches) }

    override fun equals(other: Any?): Boolean {
        if (other !is AnnotationFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = regex.hashCode()

    override fun toString(): String {
        return "AnnotationFilter(regex=$regex)"
    }
}

data class TestMethodFilter(@JsonProperty("regex") val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches(it.method) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches(it.method) }

    override fun equals(other: Any?): Boolean {
        if (other !is TestMethodFilter) return false
        return regex.toString().contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = regex.hashCode()

    override fun toString(): String {
        return "TestMethodFilter(regex=$regex)"
    }
}

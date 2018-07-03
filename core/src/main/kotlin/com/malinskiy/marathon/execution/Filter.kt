package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

interface TestFilter {
    fun filter(tests: List<Test>): List<Test>
    fun filterNot(tests: List<Test>): List<Test>
}

class SimpleClassnameFilter(val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches(it.clazz) }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches(it.clazz) }
}

class FullyQualifiedClassnameFilter(val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches("${it.pkg}${it.clazz}") }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches("${it.pkg}${it.clazz}") }
}

class TestPackageFilter(val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { regex.matches("${it.pkg}") }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { regex.matches("${it.pkg}") }
}

class AnnotationFilter(val regex: Regex) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> = tests.filter { it.annotations.any { regex.matches("$it") } }
    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { it.annotations.any { regex.matches("$it") } }
}

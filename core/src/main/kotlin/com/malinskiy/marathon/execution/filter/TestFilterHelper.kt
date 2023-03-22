package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.test.Test

fun applyTestFilters(
    parsedTests: List<Test>,
    testClassRegexes: Collection<Regex>,
    allowlist: Collection<TestFilterConfiguration>,
    blocklist: Collection<TestFilterConfiguration>
): List<Test> {
    val allowRegex = (if (testClassRegexes.isNotEmpty()) parsedTests.filter { test ->
        testClassRegexes.any { it.matches(test.clazz) }
    } else parsedTests).toSet()

    val allowTests = (if (allowlist.isNotEmpty())
        allowlist.map { it.toTestFilter().filter(parsedTests) }
            .reduce { first, second -> first + second }.distinct()
    else parsedTests).toSet()

    val blockedTest = if (blocklist.isNotEmpty()) {
        blocklist.map { it.toTestFilter().filter(parsedTests) }
            .reduce { first, second -> first + second }.distinct().toSet()
    } else emptySet()

    return parsedTests
        .intersect(allowRegex)
        .intersect(allowTests)
        .subtract(blockedTest)
        .toList()
}

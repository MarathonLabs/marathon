package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.test.Test
import java.io.File

data class SimpleTestNameFromFileFilter(val cnf: TestFilterConfiguration.SimpleTestNameFromFileFilterConfiguration) : TestFilter {

    var testFileContent: String = readFileAsLinesUsingBufferedReader(cnf.fileName)[0]

    override fun filter(tests: List<Test>): List<Test> {
        if (testFileContent.length > 5) {
            return tests.filter {
                testFileContent.contains(it.clazz)
            }
        } else if (cnf.regex != null) {
            println("No file content found. Running by annotation $cnf.regex")
            return tests.filter { it.metaProperties.map { it.name }.any(cnf.regex!!::matches) }
        } else {
            println("No regex found. Running all tests")
            return tests
        }
    }

    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot { testFileContent.contains(it.clazz) }

    override fun equals(other: Any?): Boolean {
        if (other !is SimpleClassnameFilter) return false
        return cnf.fileName.contentEquals(other.regex.toString())
    }

    override fun hashCode(): Int = testFileContent.hashCode()

    override fun toString(): String {
        return "SimpleClassnameFilterFromFile(fileName=${cnf.fileName})"
    }
}

fun readFileAsLinesUsingBufferedReader(fileName: String): List<String> = File(fileName).bufferedReader().readLines()


package com.malinskiy.marathon.vendor.junit4.booter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import org.junit.runner.Description
import org.junit.runner.JUnitCore
import org.junit.runner.Request
import org.junit.runner.manipulation.Filter

fun main(args: Array<String>) = Run().main(args)

class Run : CliktCommand() {
    val tests: List<String> by argument().multiple()

    override fun run() {
        val core = JUnitCore()
        val classLoader = ClassLoader.getSystemClassLoader()

        val klasses = mutableSetOf<Class<*>>()
        val testDescriptions = tests.map { fqtn ->
            val klass = fqtn.substringBefore('#')
            val loadClass = Class.forName(klass)
            klasses.add(loadClass)

            val method = fqtn.substringAfter('#')
            Description.createTestDescription(loadClass, method)
        }.toHashSet()

        val request = Request.classes(*klasses.toTypedArray()).filterWith(TestFilter(testDescriptions))

        val result = core.run(request)
        println(
            """
            Success: ${result.wasSuccessful()}
            Tests: ${result.runCount}
            Ignored:             ${result.ignoreCount}
            Failures: ${result.failureCount}
            ${result.failures.joinToString("\n") { "${it.description.displayName}: ${it.message}" }}
            """.trimIndent()
        )
    }
}

class TestFilter(private val testDescriptions: HashSet<Description>) : Filter() {
    override fun shouldRun(description: Description): Boolean {
        if (description.isTest) {
            return testDescriptions.contains(description)
        }

        // explicitly check if any children want to run
        for (each in description.children) {
            if (shouldRun(each!!)) {
                return true
            }
        }
        return false
    }

    override fun describe() = "Marathon JUnit4 execution filter"
}

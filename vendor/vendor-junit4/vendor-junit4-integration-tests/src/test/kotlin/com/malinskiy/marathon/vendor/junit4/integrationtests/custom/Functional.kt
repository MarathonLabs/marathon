package com.malinskiy.marathon.vendor.junit4.integrationtests.custom

import org.junit.Assert
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.junit.runners.model.Statement
import org.junit.runners.model.TestClass
import java.io.IOException
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.util.Collections
import java.util.Properties

class Functional(klass: Class<*>) : ParentRunner<Functional.Test>(klass) {
    private val tests: List<Test>

    override fun getChildren(): List<Test> {
        return tests
    }

    override fun describeChild(child: Test): Description {
        return child.description()
    }

    override fun runChild(child: Test, notifier: RunNotifier) {
        runLeaf(child, child.description(), notifier)
    }

    class Factory constructor(klass: Class<*>) {
        val testClass: TestClass
        val properties: Map<String, String>
        fun createTests(): List<Test> {
            return properties.keys
                .filter { it.startsWith("input") }
                .map { it.substringAfter("input.") }
                .map { baseTestName ->
                    createFunctionalTest(
                        baseTestName
                    )
                }.toList()
        }

        private fun createFunctionalTest(baseTestName: String): Test {
            return Test(this, baseTestName, false)
        }

        companion object {
            private fun getTestPropertiesName(testClass: TestClass): String {
                val annotation = testClass.getAnnotation(Properties::class.java)
                    ?: throw AssertionError("@Properties: required class annotation not found")
                return annotation.value
            }
        }

        init {
            testClass = TestClass(klass)
            properties = readProperties(getTestPropertiesName(testClass))
        }
    }

    class Test internal constructor(factory: Factory, testName: String, withEscaping: Boolean) : Statement() {
        private val input: String
        private val expected: String
        private val description: Description
        override fun evaluate() {
            Assert.assertEquals(input.capitalize(), expected)
        }

        fun description(): Description {
            return description
        }

        companion object {
            private fun property(factory: Factory, key: String): String {
                return factory.properties[key] ?: throw AssertionError("Property $key not found ")
            }
        }

        init {
            input = property(factory, "input.$testName")
            expected = property(factory, "output.$testName")
            description = Description.createTestDescription(
                factory.testClass.javaClass,
                testName + if (withEscaping) " escaped" else " raw"
            )
        }
    }

    /**
     * Annotation for the `public static final String` field that declares the properties file to use.
     * This annotation is required.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
    annotation class Properties(
        /**
         * The name of the properties file to load.
         *
         * @return The name of the `.properties` file that defines the individual tests
         */
        val value: String
    )

    companion object {
        private const val PROP_PREFIX = "/functional-tests/"
        private const val PROP_SUFFIX = "-functional-tests.properties"
        private fun readProperties(testsFile: String): Map<String, String> {
            try {
                Functional::class.java.getResourceAsStream(PROP_PREFIX + testsFile + PROP_SUFFIX).use { stream ->
                    if (stream == null) {
                        throw AssertionError("Could not load tests for $testsFile")
                    }
                    val properties = Properties()
                    properties.load(stream)
                    val map: MutableMap<String, String> = HashMap()
                    properties.forEach { k: Any, v: Any ->
                        map[k as String] = v as String
                    }
                    return map
                }
            } catch (e: IOException) {
                throw AssertionError("Error loading tests for $testsFile", e)
            }
        }
    }

    /**
     * Only called reflectively. Do not use programmatically.
     */
    init {
        tests = Collections.unmodifiableList(Factory(klass).createTests())
    }
}

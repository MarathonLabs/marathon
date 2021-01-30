package com.malinskiy.marathon.execution

import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.test.Test

sealed class TestFilter {
    abstract fun filter(tests: List<Test>): List<Test>
    abstract fun filterNot(tests: List<Test>): List<Test>

    data class SimpleClassnameFilter(val regex: Regex) : TestFilter() {
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

    data class FullyQualifiedClassnameFilter(val regex: Regex) : TestFilter() {
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

    data class TestPackageFilter(val regex: Regex) : TestFilter() {
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

    data class AnnotationFilter(val regex: Regex) : TestFilter() {
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

    data class TestMethodFilter(val regex: Regex) : TestFilter() {
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

    class CompositionFilter(
        private val filters: List<TestFilter>,
        private val op: OPERATION
    ) : TestFilter() {
        override fun filter(tests: List<Test>): List<Test> {
            return when (op) {
                OPERATION.UNION -> filterWithUnionOperation(tests)
                OPERATION.INTERSECTION -> filterWithIntersectionOperation(tests)
                OPERATION.SUBTRACT -> filterWithSubtractOperation(tests)
            }
        }

        override fun filterNot(tests: List<Test>): List<Test> {
            val filteredTests = filter(tests)
            return when (op) {
                OPERATION.UNION -> tests.subtract(filteredTests).toList()
                OPERATION.INTERSECTION -> tests.subtract(filteredTests).toList()
                OPERATION.SUBTRACT -> tests.subtract(filteredTests).toList()
            }
        }

        private fun filterWithUnionOperation(tests: List<Test>): List<Test> {
            return filters.fold(emptySet<Test>()) { acc, f ->
                acc.union(f.filter(tests))
            }.toList()
        }

        private fun filterWithIntersectionOperation(tests: List<Test>): List<Test> {
            return filters.fold(tests.toSet()) { acc, f ->
                acc.intersect(f.filter(tests))
            }.toList()
        }


        private fun filterWithSubtractOperation(tests: List<Test>): List<Test> {
            return filters.fold(tests.toSet()) { acc, f ->
                acc.subtract(f.filter(tests))

            }.toList()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is CompositionFilter) return false
            if (filters.count() != other.filters.count()) return false
            if (op != other.op) return false
            filters.forEach {
                if (!other.filters.contains(it)) return false
            }
            return true
        }

        override fun hashCode(): Int = filters.hashCode() + op.hashCode()

        enum class OPERATION {
            UNION,
            INTERSECTION,
            SUBTRACT
        }
    }
}



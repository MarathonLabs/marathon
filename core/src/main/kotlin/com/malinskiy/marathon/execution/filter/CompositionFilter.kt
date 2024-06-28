package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.test.Test

class CompositionFilter(
    private val filters: List<TestFilter>,
    private val op: TestFilterConfiguration.CompositionFilterConfiguration.OPERATION,
    val enabled: Boolean
) : TestFilter {

    override fun filter(tests: List<Test>): List<Test> = if (enabled) {
        when (op) {
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.UNION -> filterWithUnionOperation(tests)
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.INTERSECTION -> filterWithIntersectionOperation(tests)
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.SUBTRACT -> filterWithSubtractOperation(tests)
        }
    } else {
        tests
    }

    override fun filterNot(tests: List<Test>): List<Test> = if (enabled) {
        val filteredTests = filter(tests)
        when (op) {
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.UNION -> tests.subtract(filteredTests).toList()
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.INTERSECTION -> tests.subtract(filteredTests).toList()
            TestFilterConfiguration.CompositionFilterConfiguration.OPERATION.SUBTRACT -> tests.subtract(filteredTests).toList()
        }
    } else {
        tests
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
        return when (filters.size) {
            0, 1 -> tests
            else -> {
                val left = filters.first().filter(tests)
                filters.slice(1..filters.size - 1).fold(left.toSet()) { acc, f ->
                    acc.subtract(f.filter(left))

                }.toList()
            }
        }
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
}

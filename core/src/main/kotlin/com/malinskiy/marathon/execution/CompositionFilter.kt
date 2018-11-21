package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

class CompositionFilter(private val filters: List<TestFilter>, private val op: OPERATION) : TestFilter {
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

    enum class OPERATION {
        UNION,
        INTERSECTION,
        SUBTRACT
    }
}
package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

class CompositionFilter(private val filters: List<TestFilter>, private val op: OPERATION) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> {
        return when (op) {
            OPERATION.UNION -> filterWithUnionOperation(tests)
            OPERATION.INTERSECTION -> filterWithIntersectionOperation(tests)
            OPERATION.SUBTRACT -> filterWithSubstractOperation(tests)
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
        var filteredTests = filters[0].filter(tests)
        filters.drop(1).forEach {
            filteredTests = filteredTests.union(it.filter(tests)).toList()
        }
        return filteredTests
    }

    private fun filterWithIntersectionOperation(tests: List<Test>): List<Test> {
        var filteredTests = filters[0].filter(tests)
        filters.drop(1).forEach {
            filteredTests = tests.intersect(it.filter(filteredTests)).toList()
        }
        return filteredTests
    }


    private fun filterWithSubstractOperation(tests: List<Test>): List<Test> {
        var filteredTests = filters[0].filter(tests)
        filters.drop(1).forEach {
            filteredTests = tests.subtract(it.filter(filteredTests)).toList()
        }
        return filteredTests
    }

    enum class OPERATION {
        UNION,
        INTERSECTION,
        SUBTRACT
    }
}
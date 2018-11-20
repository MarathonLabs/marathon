package com.malinskiy.marathon.execution

import com.malinskiy.marathon.test.Test

class CompositionFilter(val filters: Array<TestFilter>, private val op: OPERATION) : TestFilter {
    override fun filter(tests: List<Test>): List<Test> {
        var initial = filters[0].filter(tests)
        filters.forEach {
            initial = when (op) {
                OPERATION.UNION -> initial.union(it.filter(tests)).toList()
                OPERATION.INTERSECTION -> initial.intersect(it.filter(tests)).toList()
            }
        }
        return initial
    }

    override fun filterNot(tests: List<Test>): List<Test> {
        var initial = filters[0].filterNot(tests)
        filters.forEach {
            initial = when (op) {
                OPERATION.UNION -> initial.union(it.filterNot(tests)).toList()
                OPERATION.INTERSECTION -> initial.intersect(it.filterNot(tests)).toList()
            }
        }
        return initial
    }

    enum class OPERATION {
        UNION,
        INTERSECTION
    }
}
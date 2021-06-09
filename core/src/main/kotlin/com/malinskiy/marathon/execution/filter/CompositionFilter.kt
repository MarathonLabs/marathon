package com.malinskiy.marathon.execution

import com.google.gson.annotations.SerializedName
import com.malinskiy.marathon.test.Test

class CompositionFilter(
    @SerializedName("filters") private val filters: List<TestFilter>,
    @SerializedName("op") private val op: OPERATION
) : TestFilter {
    override fun validate() = filters.forEach { it.validate() }

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

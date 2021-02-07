package com.malinskiy.marathon.cli.schema

sealed class TestFilter {
    data class SimpleClassname(val simpleClassname: Regex) : TestFilter()
    data class FullyQualifiedClassname(val fullyQualifiedClassname: Regex) : TestFilter()
    data class TestPackage(val `package`: Regex) : TestFilter()
    data class Annotation(val annotation: Regex) : TestFilter()
    data class TestMethod(val method: Regex) : TestFilter()
    data class Composition(val filters: List<TestFilter>, val op: OPERATION) : TestFilter()
    enum class OPERATION {
        UNION,
        INTERSECTION,
        SUBTRACT;
    }
}

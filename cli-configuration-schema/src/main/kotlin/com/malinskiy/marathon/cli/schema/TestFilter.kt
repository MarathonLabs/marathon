package com.malinskiy.marathon.cli.schema

sealed class TestFilter {
    data class SimpleClassname(val regex: Regex) : TestFilter()
    data class FullyQualifiedClassname(val regex: Regex) : TestFilter()
    data class TestPackage(val regex: Regex) : TestFilter()
    data class Annotation(val regex: Regex) : TestFilter()
    data class TestMethod(val regex: Regex) : TestFilter()
}

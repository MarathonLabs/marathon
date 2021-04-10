package com.malinskiy.marathon.cli.schema

import java.io.Serializable

sealed class TestFilter : Serializable {
    data class SimpleClassname(val regex: Regex) : TestFilter()
    data class FullyQualifiedClassname(val regex: Regex) : TestFilter()
    data class TestPackage(val regex: Regex) : TestFilter()
    data class Annotation(val regex: Regex) : TestFilter()
    data class TestMethod(val regex: Regex) : TestFilter()
}

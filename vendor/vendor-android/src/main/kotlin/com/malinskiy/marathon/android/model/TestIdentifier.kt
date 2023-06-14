package com.malinskiy.marathon.android.model

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

data class TestIdentifier(
    val className: String,
    val testName: String
) {
    fun toTest(metaProperties: List<MetaProperty> = emptyList()): Test {
        val pkg = className.substringBeforeLast(".", "")
        val className = className.substringAfterLast(".", className)
        val methodName = testName
        return Test(pkg, className, methodName, metaProperties)
    }
}

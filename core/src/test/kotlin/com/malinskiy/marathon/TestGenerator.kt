package com.malinskiy.marathon

import com.malinskiy.marathon.execution.ComponentInfo
import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestComponentInfo

fun generateTest(
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    annotations: List<MetaProperty> = emptyList(),
    componentInfo: ComponentInfo = TestComponentInfo("test")
) = Test(pkg, clazz, method, annotations, componentInfo)

fun generateTests(
    count: Int,
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    annotations: List<MetaProperty> = emptyList(),
    componentInfo: ComponentInfo = TestComponentInfo("test")
): List<Test> {
    return (0 until count).map {
        Test("$pkg$it", "$clazz$it", "$method$it", annotations, componentInfo)
    }
}
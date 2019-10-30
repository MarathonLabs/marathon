package com.malinskiy.marathon

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

fun generateTest(
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    annotations: List<MetaProperty> = emptyList()
) = Test(pkg, clazz, method, annotations)

fun generateTests(
    count: Int,
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    annotations: List<MetaProperty> = emptyList()
): List<Test> {
    return (0 until count).map {
        Test("$pkg$it", "$clazz$it", "$method$it", annotations)
    }
}
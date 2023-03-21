package com.malinskiy.marathon

import com.malinskiy.marathon.test.MetaProperty
import com.malinskiy.marathon.test.Test

fun generateTest(
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    metaProperties: List<MetaProperty> = emptyList()
) = Test(pkg, clazz, method, metaProperties)

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
fun generateClassGroupTests(
    classCounts: Int,
    methodCounts: Int,
    pkg: String = "pkg",
    clazz: String = "clazz",
    method: String = "method",
    annotations: List<MetaProperty> = emptyList()
): List<Test> {
    val tests = arrayListOf<Test>()
    for (classNumber in 0 until classCounts)
        for (methodNumber in 0 until methodCounts)
            tests.add(Test(pkg, "$clazz$classNumber", "$method$methodNumber", annotations))
    return tests
}

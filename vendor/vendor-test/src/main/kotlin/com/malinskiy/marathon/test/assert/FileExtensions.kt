package com.malinskiy.marathon.test.assert

import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.File

fun File.shouldBeEqualToAsJson(expected: File) {
    JSONAssert.assertEquals(expected.readText(), readText(), JSONCompareMode.LENIENT)
}
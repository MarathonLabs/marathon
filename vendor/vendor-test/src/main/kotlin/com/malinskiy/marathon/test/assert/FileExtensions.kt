package com.malinskiy.marathon.test.assert

import org.hamcrest.MatcherAssert
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.xmlunit.builder.Input
import org.xmlunit.matchers.CompareMatcher.isIdenticalTo
import java.io.File

fun File.shouldBeEqualToAsJson(expected: File) {
    JSONAssert.assertEquals(expected.readText(), readText(), JSONCompareMode.LENIENT)
}

fun File.shouldBeEqualToAsXML(expected: File) {
    MatcherAssert.assertThat(this, isIdenticalTo(Input.fromFile(expected)).ignoreWhitespace())
}

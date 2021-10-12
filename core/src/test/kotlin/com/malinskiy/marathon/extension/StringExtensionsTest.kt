package com.malinskiy.marathon.extension

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class StringExtensionsTest {
    @Test
    fun testStringEscaping() {
        "com.example.MyTest#method[1 - some parameters]".escape() shouldBeEqualTo "com.example.MyTest#method-1---some-parameters-"
    }
}

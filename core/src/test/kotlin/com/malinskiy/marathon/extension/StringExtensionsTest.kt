package com.malinskiy.marathon.extension

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class StringExtensionsTest {
    @Test
    fun testStringEscaping() {
        // `#` collapses to `-` (removed from the escape allow-list) so no
        // artifact path carries it — Chromium's file:// loader mishandles
        // URL-encoded `#` in <video>/<img> src. See String.escape().
        "com.example.MyTest#method[1 - some parameters]".escape() shouldBeEqualTo "com.example.MyTest-method-1---some-parameters-"
    }
}

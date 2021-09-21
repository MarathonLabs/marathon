package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Ignore
import org.junit.Test

@Ignore
class IgnoredTest {
    @Test
    fun testIgnoredTest() {
        assert(false) { "should be ignored" }
    }
}

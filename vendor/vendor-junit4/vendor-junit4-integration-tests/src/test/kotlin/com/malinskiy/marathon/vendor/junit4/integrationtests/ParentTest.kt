package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Test

open class ParentTest {
    @Test
    fun testPasses() {
        Assert.assertTrue(true)
    }
}

package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Test

class ChildTest : ParentTest() {
    @Test
    fun testChildPassed() {
        Assert.assertTrue(true)
    }
}

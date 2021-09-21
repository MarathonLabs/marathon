package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Assume.assumeFalse
import org.junit.Ignore
import org.junit.Test

class SimpleTest {
    @Test
    fun testSucceeds() {
        Assert.assertTrue(true)
    }

    @Test
    fun testFails() {
        Assert.fail("Expected failure")
    }

    @Test
    fun testFailsWithNoMessage() {
        Assert.fail()
    }

    @Test
    fun testAssumptionFails() {
        assumeFalse(true)
    }

    @Ignore
    @Test
    fun testIgnored() {
        Assert.fail("Should not happen")
    }
}

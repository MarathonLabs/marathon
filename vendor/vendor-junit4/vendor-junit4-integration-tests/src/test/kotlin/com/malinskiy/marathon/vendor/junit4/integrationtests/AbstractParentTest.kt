package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Before
import org.junit.Test

abstract class AbstractParentTest {
    protected lateinit var someParameter: String

    @Before
    fun setup() {
        someParameter = "fake"
    }

    @Test
    fun testParent() {
        Assert.assertEquals(someParameter, "fake")
    }
}

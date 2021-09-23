package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Test

class ChildFromAbstractTest : AbstractParentTest() {
    @Test
    fun testParentDidSetup() {
        Assert.assertEquals(someParameter, "fake")
    }
}

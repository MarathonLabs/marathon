package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Assert
import org.junit.Test

class JavaOptionsTest {
    @Test
    fun testSystemProperty() {
        val fooProperty = System.getProperty("foo.property")
        Assert.assertTrue(
            "incorrect foo.property; " +
                "Marathon should have passed this in correctly",
            "foo foo/foo/bar/1.0" == fooProperty
        )
    }
}

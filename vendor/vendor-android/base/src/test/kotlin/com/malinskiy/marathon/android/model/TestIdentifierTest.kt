package com.malinskiy.marathon.android.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class TestIdentifierTest {
    @Test
    fun testToTest() {
        val id = TestIdentifier("com.example.Class", "method")
        assertThat(id.toTest())
            .isEqualTo(MarathonTest("com.example", "Class", "method", emptyList()))

        assertThat(id.className).isEqualTo("com.example.Class")
        assertThat(id.testName).isEqualTo("method")
    }

    @Test
    fun testNoPackage() {
        val id = TestIdentifier("Class under test", "method")
        assertThat(id.toTest())
            .isEqualTo(MarathonTest("", "Class under test", "method", emptyList()))

        assertThat(id.className).isEqualTo("Class under test")
        assertThat(id.testName).isEqualTo("method")
    }
}

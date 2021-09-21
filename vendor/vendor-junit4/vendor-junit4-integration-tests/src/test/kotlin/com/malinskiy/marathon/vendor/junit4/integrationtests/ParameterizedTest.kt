package com.malinskiy.marathon.vendor.junit4.integrationtests

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.util.Arrays

@RunWith(Parameterized::class)
class ParameterizedTest(private val input: String, private val expected: String) {

    @Test
    fun testShouldCapitalize() {
        assert(input.capitalize() == expected)
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} -> {1}")
        fun scenarios(): Collection<Array<Any>>? {
            return Arrays.asList(
                arrayOf("a", "A"),
                arrayOf("b", "B"),
            )
        }
    }
}

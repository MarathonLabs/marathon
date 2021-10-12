package com.malinskiy.marathon.android.extension

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.malinskiy.marathon.test.MetaProperty
import org.junit.jupiter.api.Test
import com.malinskiy.marathon.test.Test as MarathonTest

class TestKtTest {
    @Test
    fun testIgnored() {
        assertThat(
            MarathonTest("com.example", "Clazz", "method", listOf(MetaProperty("org.junit.Ignore"))).isIgnored()
        ).isTrue()

        assertThat(MarathonTest("com.example", "Clazz", "method", listOf()).isIgnored())
            .isFalse()
    }
}

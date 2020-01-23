package com.example

import androidx.test.rule.ActivityTestRule
import io.qameta.allure.android.step
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore
class ClassIgnoredTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testAlwaysIgnored() {
            fail("Should've been ignored because of @Ignore on a class")
    }
}

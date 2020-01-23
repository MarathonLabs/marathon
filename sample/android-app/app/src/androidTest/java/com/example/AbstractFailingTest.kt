package com.example

import androidx.test.rule.ActivityTestRule
import io.qameta.allure.android.step
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AbstractFailingTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testAlwaysFailing() {
            assertTrue(false)
    }
}

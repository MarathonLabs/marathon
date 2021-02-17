package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TestWithWrongName {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testAlwaysFailing() {
        assertTrue(false)
    }
}

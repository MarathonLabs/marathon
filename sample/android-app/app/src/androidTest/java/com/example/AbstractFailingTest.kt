package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AbstractFailingTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testAlwaysFailing() {
        assertTrue(false)
    }
}

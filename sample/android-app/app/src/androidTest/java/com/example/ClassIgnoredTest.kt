package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore
class ClassIgnoredTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testAlwaysIgnored() {
        fail("Should've been ignored because of @Ignore on a class")
    }
}

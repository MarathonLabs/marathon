package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

class BeforeTestFailureTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testThatWillNotSeeTheLightOfDay() {
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup() {
            throw RuntimeException("Simulating failure")
        }
    }
}

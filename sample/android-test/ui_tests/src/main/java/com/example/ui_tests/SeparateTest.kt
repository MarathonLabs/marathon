package com.example.ui_tests

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.example.app_horizont_impl.AppHorizontActivity
import com.example.app_impl.AppActivity
import org.junit.Rule
import org.junit.Test

class SeparateTest {

//    @get:Rule
    val verticalScenarioRule = ActivityScenarioRule(AppActivity::class.java)

    @get:Rule
    val horizontalScenarioRule = ActivityScenarioRule(AppHorizontActivity::class.java)

    @Test
    fun test() {
        Thread.sleep(10_000)
    }
}
package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.runner.screenshot.Screenshot
import com.malinskiy.adam.junit4.android.screencapture.AdamScreenCaptureRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("General")
@Feature("Graphics on main screen")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
class ScreenshotTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)
    
    @get:Rule
    val screencaptureRule = AdamScreenCaptureRule()

    val screen = MainScreen()

    @Test
    fun testScreencapture() {
        screen {
            text {
                Screenshot.capture().process()
                hasText("Test")
            }
        }
    }
}

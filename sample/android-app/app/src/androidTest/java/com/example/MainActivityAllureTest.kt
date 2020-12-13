package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.android.allureScreenshot
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.rules.WindowHierarchyRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import io.qameta.allure.kotlin.Allure.step
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("General")
@Feature("Text on main screen")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
class MainActivityAllureTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val screenshotRule = ScreenshotRule(mode = ScreenshotRule.Mode.FAILURE, screenshotName = "ss_end")

    @get:Rule
    val logcatRule = LogcatRule()

    @get:Rule
    val windowHierarchyRule = WindowHierarchyRule()

    val screen = MainScreen()

    @Test
    fun testText() {
        step("first step") {
            Thread.sleep(500)
        }

        screen {
            text {
                step("second step") {
                    hasText("Test")

                    allureScreenshot("MainActivityAllureTest")
                }
            }
        }
    }

    @Test
    fun testFailure() {
        screen {
            text {
                step("first step") {
                    hasText("Cafebabe")
                }
            }
        }
    }
}

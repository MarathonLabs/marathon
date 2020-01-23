package com.example

import androidx.test.rule.ActivityTestRule
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.qameta.allure.android.softly
import io.qameta.allure.android.step
import io.qameta.allure.espresso.FailshotRule
import io.qameta.allure.espresso.LogcatClearRule
import io.qameta.allure.espresso.LogcatDumpRule
import io.qameta.allure.espresso.WindowHierarchyRule
import io.qameta.allure.espresso.deviceScreenshot
import org.hamcrest.core.IsEqual
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain

@Epic("General")
@Feature("Text on main screen")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
class MainActivityAllureTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    val failshot = FailshotRule()

    @Rule
    @JvmField
    val ruleChain = RuleChain.outerRule(LogcatClearRule()).around(LogcatDumpRule())

    @Rule
    @JvmField
    val windowHierarchyRule = WindowHierarchyRule()

    val screen = MainScreen()

    @Test
    fun testText() {
        step("first step") {
            Thread.sleep(500)

            softly {
                checkThat("softAssert", true, IsEqual(false))
            }
        }

        screen {
            text {
                step("second step") {
                    hasText("Test")

                    deviceScreenshot("MainActivityAllureTest")
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

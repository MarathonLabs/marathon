package com.example

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Epic("Marathon")
@Feature("Slow")
@Owner("user1")
@Severity(SeverityLevel.BLOCKER)
@Story("Slow")
@RunWith(AndroidJUnit4::class)
class MainActivitySlowTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testTextSlow() {
        java.lang.Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow1() {
        java.lang.Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow2() {
        java.lang.Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow3() {
        java.lang.Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow4() {
        java.lang.Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }
}

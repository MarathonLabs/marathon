package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Marathon")
@Feature("Slow")
@Owner("user1")
@Severity(SeverityLevel.BLOCKER)
@Story("Slow")
class MainActivitySlowTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testTextSlow() {
        Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow1() {
        Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow2() {
        Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow3() {
        Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testTextSlow4() {
        Thread.sleep(5000)
        screen {
            text {
                hasText("Test")
            }
        }
    }
}

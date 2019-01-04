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

@Epic("General")
@Feature("Text on main screen")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testText() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText1() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText2() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText3() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText4() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText5() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText6() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText7() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText8() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }

    @Test
    fun testText9() {
        java.lang.Thread.sleep(500)
        screen {
            text {
                hasText("Test")
            }
        }
    }
}

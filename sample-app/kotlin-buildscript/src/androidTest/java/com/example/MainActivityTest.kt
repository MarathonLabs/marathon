package com.example

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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

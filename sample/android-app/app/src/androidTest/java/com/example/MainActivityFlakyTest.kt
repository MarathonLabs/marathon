package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AllureAndroidJUnit4::class)
@Epic("Marathon")
@Feature("Flakiness")
@Owner("user2")
@Severity(SeverityLevel.BLOCKER)
@Story("Flaky")
class MainActivityFlakyTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    val screen = MainScreen()

    @get:Rule
    val logcatRule = ScreenshotRule(mode = ScreenshotRule.Mode.END, screenshotName = "ss_end")

    @Test
    fun testTextFlaky() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky1() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky2() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky3() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky4() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky5() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky6() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky7() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testTextFlaky8() {
        Thread.sleep(100)
        assertTrue(Random().nextBoolean())
    }
}

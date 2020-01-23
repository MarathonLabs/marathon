package com.example

import androidx.test.rule.ActivityTestRule
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.*

@Epic("Marathon")
@Feature("Flakiness")
@Owner("user2")
@Severity(SeverityLevel.BLOCKER)
@Story("Flaky")
class MainActivityFlakyTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

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

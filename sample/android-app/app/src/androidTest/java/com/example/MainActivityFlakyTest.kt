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
import org.junit.Assert.assertTrue
import java.util.Random

@Epic("Marathon")
@Feature("Flakiness")
@Owner("user2")
@Severity(SeverityLevel.BLOCKER)
@Story("Flaky")
@RunWith(AndroidJUnit4::class)
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

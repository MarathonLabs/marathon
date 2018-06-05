package com.example

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertTrue
import java.util.Random

@RunWith(AndroidJUnit4::class)
class MainActivityFlakyTest {

    @Rule
    @JvmField
    val rule = ActivityTestRule(MainActivity::class.java)

    val screen = MainScreen()

    @Test
    fun testText() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText1() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText2() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText3() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText4() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText5() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText6() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText7() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText8() {
        assertTrue(Random().nextBoolean())
    }

    @Test
    fun testText9() {
        assertTrue(Random().nextBoolean())
    }
}

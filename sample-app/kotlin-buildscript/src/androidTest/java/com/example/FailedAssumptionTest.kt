package com.example

import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.Ignore
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FailedAssumptionTest {
    @Test
    fun failedAssumptionTest() {
        assumeTrue(false)
    }

    @Test
    @Ignore
    fun ignoreTest(){

    }
}

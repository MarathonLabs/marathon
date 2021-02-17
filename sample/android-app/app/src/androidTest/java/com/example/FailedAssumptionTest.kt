package com.example

import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Marathon")
@Feature("Assumptions")
@Owner("user1")
@Severity(SeverityLevel.MINOR)
@Story("Skipped")
class FailedAssumptionTest {
    @Test
    fun failedAssumptionTest() {
            assumeTrue(false)
    }

    @Test
    @Ignore
    fun ignoreTest() {
    }
}

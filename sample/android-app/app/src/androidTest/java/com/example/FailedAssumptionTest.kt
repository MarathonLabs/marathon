package com.example

import android.support.test.runner.AndroidJUnit4
import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import org.junit.Test
import org.junit.Ignore
import org.junit.Assume.assumeTrue
import org.junit.runner.RunWith


@Epic("Marathon")
@Feature("Assumptions")
@Owner("user1")
@Severity(SeverityLevel.MINOR)
@Story("Skipped")
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

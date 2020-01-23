package com.example

import io.qameta.allure.Epic
import io.qameta.allure.Feature
import io.qameta.allure.Owner
import io.qameta.allure.Severity
import io.qameta.allure.SeverityLevel
import io.qameta.allure.Story
import io.qameta.allure.android.step
import org.junit.Assume.assumeTrue
import org.junit.Ignore
import org.junit.Test


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

package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.extension.toTestFilter
import com.malinskiy.marathon.test.MetaProperty
import io.qameta.allure.AllureId
import io.qameta.allure.testfilter.TestPlan
import io.qameta.allure.testfilter.TestPlanSupplier
import io.qameta.allure.testfilter.TestPlanV1_0
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Optional


class AllureFilterTest {
    private val testPlanSupplier = TestPlanSupplier {
        val plan = TestPlanV1_0()
        plan.tests = listOf(createTestCase("", "test2"), createTestCase("1", "test1"))
        Optional.of(plan)
    }

    private fun createTestCase(id: String, testName: String): TestPlanV1_0.TestCase {
        val testCase = TestPlanV1_0.TestCase()
        testCase.id = id
        testCase.selector = "com.sample.SimpleTest.$testName"
        return testCase
    }

    private val filter = AllureTestFilter(TestFilterConfiguration.AllureFilterConfiguration, testPlanSupplier)
    val test1 = stubTest(TestData("io.qameta.allure.AllureId", "1", "test1"))
    val test2 = stubTest(TestData("io.qameta.allure.AllureId", "2", "test2"))
    val test3 = stubTest(TestData("io.qameta.allure.AllureId", "3", "test3"))

    @Test
    fun test() {
        filter.filter(listOf(test1, test2, test3)) shouldBeEqualTo listOf(test1, test2)
    }

    private class TestData(
        var name: String = "",
        var value: String = "",
        var testName: String = ""
    )

    private fun stubTest(data: TestData) =
        com.malinskiy.marathon.test.Test(
            "com.sample",
            "SimpleTest",
            data.testName,
            listOf(MetaProperty(data.name, mapOf("value" to data.value)))
        )
}

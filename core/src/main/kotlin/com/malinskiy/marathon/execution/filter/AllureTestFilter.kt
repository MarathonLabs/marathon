package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.test.Test
import io.qameta.allure.kotlin.util.ResultsUtils.ALLURE_ID_LABEL_NAME
import io.qameta.allure.testfilter.FileTestPlanSupplier
import io.qameta.allure.testfilter.TestPlan
import io.qameta.allure.testfilter.TestPlanSupplier
import io.qameta.allure.testfilter.TestPlanV1_0

val ALLURE_ID_ANNOTATIONS = setOf("io.qameta.allure.AllureId", "io.qameta.allure.kotlin.AllureId")
class AllureTestFilter(val cnf: TestFilterConfiguration.AllureFilterConfiguration, private val testPlanSupplier: TestPlanSupplier = FileTestPlanSupplier()) : TestFilter {
    private val testPlan: TestPlan? by lazy {
        val optional = testPlanSupplier.supply()
        if (optional.isPresent) {
            optional.get()
        } else {
            null
        }
    }

    override fun filter(tests: List<Test>): List<Test> {
        return if (testPlan != null && testPlan is TestPlanV1_0) {
            val plan = testPlan as TestPlanV1_0
            tests.filter { test ->
                val allureId: String? = test.metaProperties.find { ALLURE_ID_ANNOTATIONS.contains(it.name) }?.values?.get("value") as? String
                plan.isSelected(allureId, test.toAllureName())
            }
        } else {
            tests
        }
    }

    override fun filterNot(tests: List<Test>): List<Test> {
        return if (testPlan != null && testPlan is TestPlanV1_0) {
            val plan = testPlan as TestPlanV1_0
            tests.filterNot { test ->
                val allureId: String? = test.metaProperties.find { it.name == ALLURE_ID_LABEL_NAME }?.values?.get("value") as? String
                plan.isSelected(allureId, test.toAllureName())
            }
        } else {
            emptyList()
        }
    }
}

private fun Test.toAllureName(): String {
    return "$pkg.$clazz.$method"
}

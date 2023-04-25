package com.malinskiy.marathon.execution.filter

import com.malinskiy.marathon.config.TestFilterConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.extension.md5
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.math.BigInteger

/**
 * This is a test filter similar to sharded test execution that AOSP provides
 * https://source.android.com/devices/tech/test_infra/tradefed/architecture/advanced/sharding
 *
 * It is intended to be used in situations where it is not possible to connect multiple execution devices to marathon, e.g.
 * CI setup that can schedule parallel jobs each containing a single execution device.
 *
 * This is a dynamic programming technique, hence the results will be sub-optimal compared to connecting multiple devices to the same test
 * run
 */
class FragmentationFilter(private val cnf: TestFilterConfiguration.FragmentationFilterConfiguration) : TestFilter {
    private val power by lazy { BigInteger.valueOf(cnf.count.toLong()) }
    private val remainder by lazy { BigInteger.valueOf(cnf.index.toLong()) }
    private val predicate: (Test) -> Boolean = {
        /**
         * Randomizing the distribution via md5
         */
        val testNumber = it.toTestName().md5()
        val testRemainder = testNumber % power
        val actualRemainder = if (testRemainder < BigInteger.ZERO) {
            testRemainder + power
        } else {
            testRemainder
        }

        actualRemainder == remainder
    }

    override fun filter(tests: List<Test>): List<Test> = tests.filter(predicate)

    override fun filterNot(tests: List<Test>): List<Test> = tests.filterNot(predicate)
}

package com.malinskiy.marathon.vendor.junit4

import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toHumanReadableTestName
import com.malinskiy.marathon.vendor.junit4.model.JUnit4TestBundle
import java.util.concurrent.ConcurrentHashMap

class Junit4TestBundleIdentifier : TestBundleIdentifier {
    private val testToBundle = ConcurrentHashMap<Test, JUnit4TestBundle>()

    override fun identify(test: Test): JUnit4TestBundle {
        return testToBundle[test]
            ?: throw ConfigurationException("Invalid test ${test.toHumanReadableTestName()}: can't locate test bundle")
    }

    fun put(test: Test, testBundle: JUnit4TestBundle) {
        testToBundle[test] = testBundle
    }
}

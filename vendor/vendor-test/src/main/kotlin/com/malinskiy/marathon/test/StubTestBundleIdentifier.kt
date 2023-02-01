package com.malinskiy.marathon.test

import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundle
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import java.util.concurrent.ConcurrentHashMap

class StubTestBundleIdentifier : TestBundleIdentifier {
    private val testToBundle = ConcurrentHashMap<Test, StubTestBundle>()
    
    fun put(test: Test, testBundle: StubTestBundle) {
        testToBundle[test] = testBundle
    }
    
    override fun identify(test: Test): TestBundle? {
        return testToBundle[test]
            ?: throw ConfigurationException("Invalid test ${test.toTestName()}: can't locate test bundle")
    }
}

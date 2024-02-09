package com.malinskiy.marathon.apple.ios

import com.malinskiy.marathon.apple.ios.model.AppleTestBundle
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName
import java.util.concurrent.ConcurrentHashMap

class AppleTestBundleIdentifier : TestBundleIdentifier {
    private val testToBundle = ConcurrentHashMap<Test, AppleTestBundle>()
    
    override fun identify(test: Test): AppleTestBundle {
        return testToBundle[test]
            ?: throw ConfigurationException("Invalid test ${test.toTestName()}: can't locate test bundle")
    }
    
    fun put(test: Test, androidTestBundle: AppleTestBundle) {
        testToBundle[test] = androidTestBundle
    }
}

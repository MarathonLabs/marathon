package com.malinskiy.marathon.android

import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.config.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toHumanReadableTestName
import java.util.concurrent.ConcurrentHashMap

class AndroidTestBundleIdentifier : TestBundleIdentifier {
    private val testToBundle = ConcurrentHashMap<Test, AndroidTestBundle>()

    override fun identify(test: Test): AndroidTestBundle {
        return testToBundle[test]
            ?: throw ConfigurationException("Invalid test ${test.toHumanReadableTestName()}: can't locate test bundle")
    }

    fun put(test: Test, androidTestBundle: AndroidTestBundle) {
        testToBundle[test] = androidTestBundle
    }
}

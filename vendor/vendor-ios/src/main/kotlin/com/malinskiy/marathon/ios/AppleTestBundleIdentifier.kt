package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test

class AppleTestBundleIdentifier : TestBundleIdentifier {
    /**
     * null means no support for multi-app testing
     */
    override fun identify(test: Test) = null
}

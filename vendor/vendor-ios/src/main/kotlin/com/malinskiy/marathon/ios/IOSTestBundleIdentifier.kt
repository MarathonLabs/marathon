package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test

class IOSTestBundleIdentifier : TestBundleIdentifier {
    override fun identify(test: Test) = null
}

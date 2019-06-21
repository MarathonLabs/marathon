package com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota

import com.malinskiy.marathon.test.Test

interface TestMatcher {
    fun matches(test: Test): Boolean
}

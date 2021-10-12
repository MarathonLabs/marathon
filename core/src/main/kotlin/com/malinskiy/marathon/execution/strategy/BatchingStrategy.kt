package com.malinskiy.marathon.execution.strategy

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import java.util.Queue

interface BatchingStrategy {
    fun process(queue: Queue<Test>, analytics: Analytics, testBundleIdentifier: TestBundleIdentifier?): TestBatch
}

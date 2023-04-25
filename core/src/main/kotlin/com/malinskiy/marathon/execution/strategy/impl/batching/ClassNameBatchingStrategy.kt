package com.malinskiy.marathon.execution.strategy.impl.batching

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.execution.bundle.TestBundleIdentifier
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toClassName
import java.util.Queue

class ClassNameBatchingStrategy : BatchingStrategy {

    override fun process(queue: Queue<Test>, analytics: Analytics, testBundleIdentifier: TestBundleIdentifier?): TestBatch {
        val classNameSelector = queue.peek().toClassName()
        val classNameTestGroup = queue.toList().filter { test -> test.toClassName() == classNameSelector }
        queue.removeAll(classNameTestGroup.toSet())
        return TestBatch(classNameTestGroup)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassNameBatchingStrategy

        return true
    }

    override fun hashCode(): Int {
        return javaClass.canonicalName.hashCode()
    }

    override fun toString(): String {
        return "ClassNameBatchingStrategy()"
    }
}

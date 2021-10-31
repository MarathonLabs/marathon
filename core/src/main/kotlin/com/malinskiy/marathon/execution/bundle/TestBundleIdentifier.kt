package com.malinskiy.marathon.execution.bundle

import com.malinskiy.marathon.test.Test

/**
 * In a multi-module testing scenario marathon is expected to execute tests for multiple modules. However, some vendors support batching
 * tests from different modules while others do not. TestBundle is essentially a key that allows to batch tests that belong to the same
 * module together.
 *
 * For vendors that support executing tests from multiple modules in a single batch you can skip providing an implementation of this.
 *
 * For vendor that do not support or have a performance penalty - please provide an implementation
 */
interface TestBundleIdentifier {
    fun identify(test: Test): TestBundle?
}

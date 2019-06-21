package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

sealed class DeviceAction {
    object Initialize : DeviceAction()
    data class Terminate(val batch: TestBatch? = null, val result: CompletableDeferred<TestBatchResults>? = null) : DeviceAction()
    data class ExecuteBatch(val batch: TestBatch, val result: CompletableDeferred<TestBatchResults>) : DeviceAction()
    data class NotifyIsReady(val result: CompletableDeferred<TestBatchResults>? = null) : DeviceAction()
}

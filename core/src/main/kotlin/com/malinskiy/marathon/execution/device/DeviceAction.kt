package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

sealed class DeviceAction {
    object Initialize : DeviceAction()
    data class ReturnBatchAndInitialize(val result: CompletableDeferred<TestBatchResults>) : DeviceAction()

    object Terminate : DeviceAction()
    data class StopAndTerminatee(val result: CompletableDeferred<TestBatchResults>) : DeviceAction()

    data class ExecuteBatch(val batch: TestBatch, val result: CompletableDeferred<TestBatchResults>) : DeviceAction()

    data class SendResultAndNotifyIsReady(val result: CompletableDeferred<TestBatchResults>) : DeviceAction()

    object NotifyIsReady : DeviceAction()
}

package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

sealed class DeviceAction {
    object Initialize : DeviceAction()
    object ReturnBatchAndInitialize : DeviceAction()

    object Terminate : DeviceAction()

    object StopAndTerminatee : DeviceAction()

    data class ExecuteBatch(val batch: TestBatch) : DeviceAction()

    object SendResultAndNotifyIsReady : DeviceAction()

    object NotifyIsReady : DeviceAction()
}

package com.malinskiy.marathon.execution.device

import com.malinskiy.marathon.test.TestBatch
import kotlinx.coroutines.CompletableDeferred

sealed class DeviceEvent {
    data class Execute(val batch: TestBatch) : DeviceEvent()
    object Complete : DeviceEvent()
    object Initialize : DeviceEvent()
    object Terminate : DeviceEvent()
    object WakeUp : DeviceEvent()
    data class GetDeviceState(val deferred: CompletableDeferred<DeviceState>) : DeviceEvent()
}

package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.DevicePool
import com.malinskiy.marathon.test.Test

data class ExecutionShard(val devicePool: DevicePool, val tests: Collection<Test>)